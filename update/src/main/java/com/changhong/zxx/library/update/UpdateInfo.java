package com.changhong.zxx.library.update;

/**
 * Copyright (c) 2017 北京视达科技有限责任公司 All rights reserved.
 *
 * @author zxx
 * @date 2017/3/17
 * @description
 */
public class UpdateInfo {
    public UpdateManager.UpdateType type = UpdateManager.UpdateType.NOUPDATE;//当前更新类型
    public String url = "";   //后台下发的更新地址，为空会去取 url_backup =”备用升级地址”
    public String app_version = "";     //升级后版本号
    public String tip;//版本说明

    @Override
    public String toString() {
        return "UpdateInfo{" +
                "type=" + type +
                ", url='" + url + '\'' +
                ", app_version='" + app_version + '\'' +
                '}';
    }
}
