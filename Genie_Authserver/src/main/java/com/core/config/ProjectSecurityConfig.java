package com.core.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
@Configuration
@EnableWebSecurity
public class ProjectSecurityConfig {


    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());    // Enable OpenID Connect 1.0
        http
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // Accept access tokens for User Info and/or Client Registration
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().authenticated()
                )
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
                .formLogin(Customizer.withDefaults());

        return http.build();
    }


    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient clientCredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("GenieClient")
                .clientSecret("{noop}VxubZgAXyyTq9lGjj3qGvWNsHtE4SqTq")
                // it describes , how the client is going to authenticate itself
                // here we are using client_secret_basic i.e. http basic auth
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // client credentials are sent in auth header
                // we can add multiple grant type flows by adding the same line multiple times

                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scopes(scopeConfig -> scopeConfig.addAll(List.of(OidcScopes.OPENID, "GENIE", "WISHMAKER")))
                // defining the token settings , how the tokens are to be generated

                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(10))
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED).build()).build();
        // to make the token opaque , we can use OAuth2TokenFormat.REFERENCE

        // Resource server as client
        RegisteredClient introspectClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("GenieIntrospectClient")
                .clientSecret("{noop}dG1gm33mXSUA5Sp7lG2lSRN9kDj1ivID")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scopes(scopeConfig -> scopeConfig.addAll(List.of(OidcScopes.OPENID)))
                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(10))
                        .accessTokenFormat(OAuth2TokenFormat.REFERENCE).build()).build();

        RegisteredClient authCodeClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("GenieAuthCodeClient")
                .clientSecret("{noop}Qw3rTy6UjMnB9zXcV2pL0sKjHn5TxQqB")
                // why we are using 2 clientAuthenticationMethod ? one for normal auth code grant type flow , and other for refreshtype grant type flow
                // as refresh type grant type flow is done using header
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST) // client credentails are sent in request body
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://oauth.pstmn.io/v1/callback")
                // these are the client scopes that the client is going to use
                // we are not mentioning the user related scopes here

                .scope(OidcScopes.OPENID).scope(OidcScopes.EMAIL)
                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(10))
                        .refreshTokenTimeToLive(Duration.ofHours(8)).reuseRefreshTokens(false)
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED).build()).build();

        RegisteredClient pkceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("GeniePkceClient")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("https://oauth.pstmn.io/v1/callback")
                // these are the client scopes that the client is going to use
                // we are not mentioning the user related scopes here

                .scope(OidcScopes.OPENID).scope(OidcScopes.EMAIL)
                // since our client doesnot have the client secret , so to allow client to send the code verifier and code challenge , we need to make requireProofKey(true)

                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(10))
                        .refreshTokenTimeToLive(Duration.ofHours(8)).reuseRefreshTokens(false)
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED).build()).build();


        // we are storing the client info in memory
        // so we can access it from any place in the application

        return new InMemoryRegisteredClientRepository(clientCredClient, introspectClient, authCodeClient, pkceClient);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }


    // it generates the public and private keys upon startup
    // these keys are used to sign the JWT tokens such as access tokens , id tokes etc.

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }


    // we are telling that whenever , you are signing any token , please sign it with jwksource (it got created upside )

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            // right now we are modifying only the access token , if you want to modify the id token , you can do it here

            if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
                // claims are the payload of the token
                // and we are adding a claim called roles

                context.getClaims().claims((claims) -> {
                    // scope(it is what clients ask for to the auth server) is for the clients and roles are for users
                    // and scope and roles are same for the clients

                    if (context.getAuthorizationGrantType().equals(AuthorizationGrantType.CLIENT_CREDENTIALS)) {
                        Set<String> roles = context.getClaims().build().getClaim("scope");
                        claims.put("roles", roles);
                    }
                    // but for other grant type flow , roles and scopes are diff.
                    else if (context.getAuthorizationGrantType().equals(AuthorizationGrantType.AUTHORIZATION_CODE) || context.getAuthorizationGrantType().equals(AuthorizationGrantType.REFRESH_TOKEN)) {
                        // getting the auth related info and then converting into the set

                        Set<String> roles = AuthorityUtils.authorityListToSet(context.getPrincipal().getAuthorities())
                                .stream()
                                .map(c -> c.replaceFirst("^ROLE_", ""))
                                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
                        claims.put("roles", roles);
                    }
                });
            }
        };
    }

    // for password encoder

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * From Spring Security 6.3 version
     *
     * @return
     */

    // for compromised password checker

    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

}
