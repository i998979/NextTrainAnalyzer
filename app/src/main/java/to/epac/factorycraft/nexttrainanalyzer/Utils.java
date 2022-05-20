package to.epac.factorycraft.nexttrainanalyzer;

import android.app.ActivityManager;
import android.content.Context;

public class Utils {

    public static String getStationName(String name) {
        if (name.equals("ADM")) return "金鐘";
        if (name.equals("EXC")) return "會展";
        if (name.equals("HUH")) return "紅磡";
        if (name.equals("MKK")) return "旺角東";
        if (name.equals("KOT")) return "九龍塘";
        if (name.equals("TAW")) return "大圍";
        if (name.equals("SHT")) return "沙田";
        if (name.equals("FOT")) return "火炭";
        if (name.equals("RAC")) return "馬場";
        if (name.equals("UNI")) return "大學";
        if (name.equals("TAP")) return "大埔墟";
        if (name.equals("TWO")) return "太和";
        if (name.equals("FAN")) return "粉嶺";
        if (name.equals("SHS")) return "上水";
        if (name.equals("LOW")) return "羅湖";
        if (name.equals("LMC")) return "落馬洲";

        if (name.equals("WKS")) return "烏溪沙";
        if (name.equals("MOS")) return "馬鞍山";
        if (name.equals("HEO")) return "恆安";
        if (name.equals("TSH")) return "大水坑";
        if (name.equals("SHM")) return "石門";
        if (name.equals("CIO")) return "第一城";
        if (name.equals("STW")) return "沙田圍";
        if (name.equals("CKT")) return "車公廟";
        if (name.equals("TAW")) return "大圍";
        if (name.equals("HIK")) return "顯徑";
        if (name.equals("DIH")) return "鑽石山";
        if (name.equals("KAT")) return "啟德";
        if (name.equals("SUW")) return "宋皇臺";
        if (name.equals("TKW")) return "土瓜灣";
        if (name.equals("HOM")) return "何文田";
        if (name.equals("HUH")) return "紅磡";
        if (name.equals("ETS")) return "尖東";
        if (name.equals("AUS")) return "柯士甸";
        if (name.equals("NAC")) return "南昌";
        if (name.equals("MEF")) return "美孚";
        if (name.equals("TWW")) return "荃灣西";
        if (name.equals("KSR")) return "錦上路";
        if (name.equals("YUL")) return "元朗";
        if (name.equals("LOP")) return "朗屏";
        if (name.equals("TIS")) return "天水圍";
        if (name.equals("SIH")) return "兆康";
        if (name.equals("TUM")) return "屯門";

        if (name.equals("HOK")) return "香港";
        if (name.equals("KOW")) return "九龍";
        if (name.equals("OLY")) return "奧運";
        if (name.equals("NAC")) return "南昌";
        if (name.equals("LAK")) return "茘景";
        if (name.equals("TSY")) return "青衣";
        if (name.equals("SUN")) return "欣澳";
        if (name.equals("TUC")) return "東涌";


        if (name.equals("NOP")) return "北角";
        if (name.equals("QUB")) return "鰂魚涌";
        if (name.equals("YAT")) return "油塘";
        if (name.equals("TIK")) return "調景嶺";
        if (name.equals("TKO")) return "將軍澳";
        if (name.equals("HAH")) return "坑口";
        if (name.equals("POA")) return "寶琳";
        if (name.equals("LHP")) return "康城";

        if (name.equals("HOK")) return "香港";
        if (name.equals("KOW")) return "九龍";
        if (name.equals("TSY")) return "青衣";
        if (name.equals("AIR")) return "機場";
        if (name.equals("AWE")) return "博覽館";

        return "紅磡";
    }

    public static boolean isScanning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ScanService.class.getName().equals(service.service.getClassName()))
                return true;
        }

        return false;
    }
}
