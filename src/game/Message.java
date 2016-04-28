/*
 * Copyright (C) 2015 joulupunikki joulupunikki@gmail.communist.invalid.
 *
 *  Disclaimer of Warranties and Limitation of Liability.
 *
 *     The creators and distributors offer this software as-is and
 *     as-available, and make no representations or warranties of any
 *     kind concerning this software, whether express, implied, statutory,
 *     or other. This includes, without limitation, warranties of title,
 *     merchantability, fitness for a particular purpose, non-infringement,
 *     absence of latent or other defects, accuracy, or the presence or
 *     absence of errors, whether or not known or discoverable.
 *
 *     To the extent possible, in no event will the creators or distributors
 *     be liable on any legal theory (including, without limitation,
 *     negligence) or otherwise for any direct, special, indirect,
 *     incidental, consequential, punitive, exemplary, or other losses,
 *     costs, expenses, or damages arising out of the use of this software,
 *     even if the creators or distributors have been advised of the
 *     possibility of such losses, costs, expenses, or damages.
 *
 *     The disclaimer of warranties and limitation of liability provided
 *     above shall be interpreted in a manner that, to the extent possible,
 *     most closely approximates an absolute disclaimer and waiver of
 *     all liability.
 *
 */
package game;

import java.io.Serializable;
import util.C;

/**
 * A diplomatic message between two factions.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class Message implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String msg_txt;
    private C.Msg type;
    private int year;
    private Object source;
    private boolean read;
    private Contract contract;

    public Message(String msg_txt, C.Msg type, int year, Object source) {
        this.msg_txt = msg_txt;
        this.type = type;
        this.year = year;
        this.source = source;
        this.read = false;
    }

    /**
     * @return the contract
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * @param contract the contract to set
     */
    public void setContract(Contract contract) {
        this.contract = contract;
    }

    /**
     * @return the msg_txt
     */
    public String getMsgTxt() {
        return msg_txt;
    }

    /**
     * @return the type
     */
    public C.Msg getType() {
        return type;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @return the source
     */
    public Object getSource() {
        return source;
    }

    /**
     * @return the read
     */
    public boolean isRead() {
        return read;
    }

    /**
     * @param read the read to set
     */
    public void setRead(boolean read) {
        this.read = read;
    }

}
