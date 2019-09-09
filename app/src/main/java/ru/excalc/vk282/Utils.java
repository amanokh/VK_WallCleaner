package ru.excalc.vk282;

public class Utils {
    public static String caseCount(int count){
        if (count%10>=5 && count%10<=9 || (count%100>=11 && count%100<=14) || count%10==0) return " записей";
        else if (count%10<=4 && count%10>=2) return " записи";
        else return " запись";
    }
}
