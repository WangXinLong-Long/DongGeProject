package com.invengo.sample;

/**
 * Created by Administrator on 2017/12/21.
 */

public class Goodbean {



        /**
         * appid : dmgb930887eadd538cc
         * dvid : master
         * access_token : ea1fac5431b0b812f84a8145a6e28eed08a9b059
         * action : get_goods_baseinfo
         * data : {"rfid":"213123135"}
         */

        private String appid;
        private String dvid;
        private String access_token;
        private String action;
        private DataBean data;

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getDvid() {
            return dvid;
        }

        public void setDvid(String dvid) {
            this.dvid = dvid;
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public DataBean getData() {
            return data;
        }

        public void setData(DataBean data) {
            this.data = data;
        }

        public static class DataBean {
            /**
             * rfid : 213123135
             */

            private String rfid;

            public String getRfid() {
                return rfid;
            }

            public void setRfid(String rfid) {
                this.rfid = rfid;
            }
        }


}
