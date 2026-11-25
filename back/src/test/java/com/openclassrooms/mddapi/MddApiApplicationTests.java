package com.openclassrooms.mddapi;

import com.openclassrooms.mddapi.config.DevDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MddApiApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
    }

    @Test
    void demoDataIsNotLoadedOutsideTheDevProfile() {
        // Comptes au mot de passe connu : jamais en dehors du profil dev
        assertThat(context.getBeanProvider(DevDataInitializer.class).getIfAvailable()).isNull();
    }

}
