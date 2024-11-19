package com.example.mybestlocation;

public class Config {
    //public static String IP="192.168.230.110";
    //public static String url_getAll="http://"+IP+":70/service%20PHP/get_all.php";

    //public static String url_add="http://"+IP+":70/service%20PHP/add_position.php";

        // Replace with your computer's local IP address for network testing
        public static String IP = "10.0.2.2"; // For Emulator
        // public static String IP = "http://192.168.x.x"; // For physical device testing (replace with actual IP)
        public static String PORT = "";
        // Complete URLs for the PHP scripts
        public static String url_getAll = "http://" + IP + "/servicephp/getall.php";
        public static String url_add = "http://" + IP + "/servicephp/add.php";


        public static String url_delete="http://" + IP + "/servicephp/delete.php";
}
