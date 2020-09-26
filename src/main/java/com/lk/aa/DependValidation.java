package com.lk.aa;

import com.lk.nex.Nex001;

public class DependValidation {

    public static void main(String[] args) {

        Nex001 nex001 = new Nex001();
        nex001.aa("aaaa ");
        Nex001.main(1111);
        Nex001.main(new String[]{"aa", "bb"});
        nex001.bb();
    }
}
