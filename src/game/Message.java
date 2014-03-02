/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.io.Serializable;
import util.C;

/**
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class Message implements Serializable {

    String msg_txt;
    C.Msg type;
    int year;
    Object source;

    public Message(String msg_txt, C.Msg type, int year, Object source) {
        this.msg_txt = msg_txt;
        this.type = type;
        this.year = year;
        this.source = source;
    }

}
