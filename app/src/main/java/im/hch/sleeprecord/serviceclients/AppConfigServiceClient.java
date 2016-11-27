package im.hch.sleeprecord.serviceclients;

import im.hch.sleeprecord.models.AppConfig;

public class AppConfigServiceClient extends BaseServiceClient {

    public AppConfig retrieveAppConfig(String packageName) {
        //TODO implement

        AppConfig appConfig = new AppConfig();
        appConfig.setSplashImageUrl("https://s-media-cache-ak0.pinimg.com/236x/ab/19/55/ab195529d317cd8d107ba3b87a4297eb.jpg");

        return appConfig;
    }
}
