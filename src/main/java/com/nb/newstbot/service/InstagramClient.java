package com.nb.newstbot.service;

import com.nb.newstbot.domain.InstaCredential;
import com.nb.newstbot.domain.enums.InstagramLoginStatus;
import com.nb.newstbot.exception.InstadaException;
import javaslang.Tuple;
import javaslang.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.brunocvcunha.instagram4j.Instagram4j;
import org.brunocvcunha.instagram4j.requests.InstagramDirectShareRequest;
import org.brunocvcunha.instagram4j.requests.InstagramGetChallengeRequest;
import org.brunocvcunha.instagram4j.requests.InstagramResetChallengeRequest;
import org.brunocvcunha.instagram4j.requests.InstagramSearchUsernameRequest;
import org.brunocvcunha.instagram4j.requests.InstagramSelectVerifyMethodRequest;
import org.brunocvcunha.instagram4j.requests.InstagramSendSecurityCodeRequest;
import org.brunocvcunha.instagram4j.requests.payload.InstagramGetChallengeResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramLoginResult;
import org.brunocvcunha.instagram4j.requests.payload.InstagramSearchUsernameResult;
import org.brunocvcunha.instagram4j.requests.payload.StatusResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Nick Barban.
 */
@Slf4j
public class InstagramClient {

//    @Autowired
//    private InstaProperties instaProperties;

    private static final Path SECURED_CODE_PATH = Paths.get("securedCodePath");

//    private final UserRepository userRepository;

    private int currentUserIndex = 0;

//    public InstagramService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

    public Instagram4j initInstagram(String username, String password) {
        Instagram4j instagram = Instagram4j.builder().username(username).password(password).build();
        instagram.setup();
        return instagram;
    }

    public Tuple2<InstagramLoginStatus, String> loginInstagram(Instagram4j instagram) throws IOException {

        final InstagramLoginResult login = instagram.login();

        if (Objects.equals(login.getStatus(), "ok")) {
            log.info("login success");
            return Tuple.of(InstagramLoginStatus.OK, "Login success");
        } else {
            log.info("Challenge should be resetted");
            if (Objects.equals(login.getError_type(), "checkpoint_challenge_required")) {
                // Challenge required

                // Get challenge URL
                String challengeUrl = login.getChallenge().getApi_path().substring(1);

                // Reset challenge
                String resetChallengeUrl = challengeUrl.replace("challenge", "challenge/reset");
                InstagramGetChallengeResult getChallengeResult = instagram.sendRequest(new InstagramResetChallengeRequest(resetChallengeUrl));

                // If action is close
                if (Objects.equals(getChallengeResult.getAction(), "close")) {
                    // Get challenge
                    getChallengeResult = instagram.sendRequest(new InstagramGetChallengeRequest(challengeUrl));
                }

                if (Objects.equals(getChallengeResult.getStep_name(), "select_verify_method")) {

                    // Get security code
                    instagram.sendRequest(new InstagramSelectVerifyMethodRequest(challengeUrl,
                            getChallengeResult.getStep_data().getChoice()));

                    log.info("input security code");
                    String securityCode = readSecureCodeFile();

                    if (StringUtils.isEmpty(securityCode)) {
                        return Tuple.of(InstagramLoginStatus.CODE, challengeUrl);
                    }

                    // Send security code
                    InstagramLoginResult securityCodeInstagramLoginResult = instagram
                            .sendRequest(new InstagramSendSecurityCodeRequest(challengeUrl, securityCode));

                    if (Objects.equals(securityCodeInstagramLoginResult.getStatus(), "ok")) {
                        log.info("login success");
                    } else {
                        return Tuple.of(InstagramLoginStatus.FAIL, login.getMessage());
                    }
                }
            }
        }
        return Tuple.of(InstagramLoginStatus.OK, "Login success");
    }

    public InstagramLoginStatus sendCode(Instagram4j instagram, String code, String challengeUrl) {
        writeSecureCodeFile(code);
        // Send security code
        // Send security code
        try {
            InstagramLoginResult securityCodeInstagramLoginResult = null;
            securityCodeInstagramLoginResult = instagram
                    .sendRequest(new InstagramSendSecurityCodeRequest(challengeUrl, code));

            if (Objects.equals(securityCodeInstagramLoginResult.getStatus(), "ok")) {
                log.info("login success");
                return InstagramLoginStatus.OK;
            } else {
                log.error("Login fail");
                return InstagramLoginStatus.FAIL;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return InstagramLoginStatus.FAIL;
        }
    }

    /*@Cacheable("followers")
    public List<Follower> getFollowers(Instagram4j instagram, long pk) throws IOException {
        log.info("Get followers for pk={}", pk);
        final InstagramGetUserFollowersResult followersResult = instagram.sendRequest(new InstagramGetUserFollowersRequest(pk));
        final List<InstagramUserSummary> followers = followersResult.getUsers();
        return followers.stream()
                .limit(50)
                .map(fol -> getUser(instagram, fol.getUsername()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }*/

    /*@SneakyThrows
    @Cacheable("user")
    public Follower getUser(Instagram4j instagram, String username) {
        return userRepository.findById(username)
                .map(Follower::from)
                .orElseGet(() -> {
                    log.info(String.format("Fetch user [%s] from API", username));
                    final InstagramSearchUsernameRequest request = new InstagramSearchUsernameRequest(username);
                    InstagramSearchUsernameResult result;
                    result = getInstagramSearchUsernameResult(instagram, username, request);

                    final InstagramUser user = result.getUser();

                    if (user == null) {
                        final InstaCredential instaCredential = instaProperties.getUsers().get(++this.currentUserIndex);
                        log.info(String.format("Try credentials for %s", instaCredential.getUsername()));
                        reconnect(instagram, instaCredential);
                        result = getInstagramSearchUsernameResult(instagram, instaCredential.getUsername(), request);

                        if (result.getUser() == null) {
                            final String errorMessage = String.format("Can not get user with username: %s", username);
                            log.error(errorMessage);
                            throw new InstadaException(errorMessage);
                        }
                    }

                    InstagramUserEntity entity = InstagramUserEntity.from(user);
                    userRepository.save(entity);
                    return Follower.from(user);
                });
    }*/

    private InstagramSearchUsernameResult getInstagramSearchUsernameResult(Instagram4j instagram, String username, InstagramSearchUsernameRequest request) {
        InstagramSearchUsernameResult result;
        try {
            result = instagram.sendRequest(request);
            int sleep = 1;
            while (result.getUser() == null && sleep < 5) {
                /*final HttpResponse lastResponse = instagram.getLastResponse();
                String headers = Arrays.stream(lastResponse.getAllHeaders())
                        .map(Object::toString)
                        .collect(Collectors.joining("\n"));
                log.info(headers);*/
                sleep *= 2;
                try {
                    log.info(String.format("Try after a pause of %d minutes", sleep));
                    TimeUnit.MINUTES.sleep(sleep);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    Thread.currentThread().interrupt();
                }
                result = instagram.sendRequest(request);
            }
        } catch (IOException e) {
            final String errorMessage = String.format("Can not get user with username: %s", username);
            log.error(errorMessage);
            throw new InstadaException(errorMessage, e);
        }
        return result;
    }

    private void reconnect(Instagram4j instagram, InstaCredential instaCredential) {
        instagram = this.initInstagram(instaCredential.getUsername(), instaCredential.getPassword());
    }

    private String readSecureCodeFile() {
        try {
            return Files.readAllLines(SECURED_CODE_PATH).get(0);
        } catch (IOException ex) {
            log.error("Can not write secure code to file");
            throw new RuntimeException(ex);
        }
    }

    private void writeSecureCodeFile(String securityCode) {
        try (BufferedWriter writer = Files.newBufferedWriter(SECURED_CODE_PATH)) {
            if (!Files.exists(SECURED_CODE_PATH)) {
                Files.createFile(SECURED_CODE_PATH);
            }
            writer.write(securityCode);
        } catch (IOException ex) {
            log.error("Can not write secure code to file");
            throw new RuntimeException(ex);
        }
    }

//    public List<Follower> fetchFollowers(int offset, int limit) {
//        return null;
//    }

    public int getFollowersCount() {
        return 0;
    }

    /*public Instagram4j initInstagram() {
        final InstaCredential initialCredential = instaProperties.getUsers().get(currentUserIndex);
        return initInstagram(initialCredential.getUsername(), initialCredential.getPassword());
    }*/

    /*public Follower getBaseUser(Instagram4j instagram) throws IOException {
        return getUser(instagram, instaProperties.getUsers().get(0).getUsername());
    }*/

    public StatusResult broadcastMessage(Instagram4j instagram, String message, List<String> recipients) throws IOException {
        final InstagramDirectShareRequest request = InstagramDirectShareRequest.builder()
                .message(message)
                .recipients(recipients)
                .shareType(InstagramDirectShareRequest.ShareType.MESSAGE)
                .build();

        final StatusResult statusResult = instagram.sendRequest(request);

        return statusResult;
    }

    public void send(String message) {
        log.info("Send to instagram: {}", message);
    }
}
