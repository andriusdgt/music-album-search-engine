package com.andriusdgt.musicalbumengine.config

import org.hsqldb.util.DatabaseManagerSwing
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
@ConditionalOnProperty("hsqldb.console.enabled")
class HSqlDbConfig {

    @PostConstruct
    fun getDbManager() {
        System.setProperty("java.awt.headless", "false")
        DatabaseManagerSwing.main(arrayOf("--url", "jdbc:hsqldb:mem:musicdb", "--user", "sa", "--password", ""))
    }
}
