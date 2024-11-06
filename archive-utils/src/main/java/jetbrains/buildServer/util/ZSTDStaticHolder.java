package jetbrains.buildServer.util;

import jetbrains.buildServer.ExtensionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ZSTDStaticHolder {

        private static ExtensionHolder extensionHolder;

        @Autowired
        private ExtensionHolder tmpHolder;

        @PostConstruct
        public void init() {
                extensionHolder = tmpHolder;
        }

        public static ExtensionHolder getExtensionHolder(){
                return extensionHolder;
        }

}
