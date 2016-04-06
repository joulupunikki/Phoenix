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
package gui;

import dat.UnitType;
import galaxyreader.Unit;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import util.WindowSize;

/**
 * Holds unit stat displays for stack window and build panel. Divided into left
 * panel, right panel and attack panel. Also holds current top display values.
 *
 * @author joulupunikki <joulupunikki@gmail.communist.invalid>
 */
public class UnitStats {

    public static class Top {

        private String xp;

        public String getXp() {
            return xp;
        }

        public void setValues(Unit u) {
            if (u == null) {
                xp = "";
                return;
            }
            setXp(u.experience);
        }

        private void setXp(int xp_int) {
            switch (xp_int) {
                case 0:
                case 1:
                case 2:
                    xp = Unit.XP.values()[xp_int].name();
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    public static class Left extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private Gui gui;
        private WindowSize ws;
        private JLabel armor_name;
        private JLabel agility_name;
        private JLabel spot_name;
        private JLabel camo_name;
        private JLabel armor_value;
        private JLabel agility_value;
        private JLabel spot_value;
        private JLabel camo_value;

        public Left(Gui gui) {
            this.gui = gui;
            ws = Gui.getWindowSize();
            setLayout(null);
            setBackground(Color.BLACK);
            armor_name = new JLabel("Armor");
            armor_name.setBounds(ws.us_s_name_x1, ws.us_s_name_y1, ws.us_s_name_w, ws.us_s_name_h);
            armor_name.setHorizontalAlignment(SwingConstants.LEFT);
            this.add(armor_name);

            agility_name = new JLabel("Agility");
            agility_name.setBounds(ws.us_s_name_x1, ws.us_s_name_y1 + ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(agility_name);
            spot_name = new JLabel("Spot");
            spot_name.setBounds(ws.us_s_name_x1, ws.us_s_name_y1 + 2 * ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(spot_name);
            camo_name = new JLabel("Camo");
            camo_name.setBounds(ws.us_s_name_x1, ws.us_s_name_y1 + 3 * ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(camo_name);
            armor_value = new JLabel();
            armor_value.setHorizontalAlignment(SwingConstants.RIGHT);
            armor_value.setBounds(ws.us_s_value_x1, ws.us_s_name_y1, ws.us_s_name_w, ws.us_s_name_h);
            this.add(armor_value);
            agility_value = new JLabel();
            agility_value.setHorizontalAlignment(SwingConstants.RIGHT);
            agility_value.setBounds(ws.us_s_value_x1, ws.us_s_name_y1 + ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(agility_value);
            spot_value = new JLabel();
            spot_value.setHorizontalAlignment(SwingConstants.RIGHT);
            spot_value.setBounds(ws.us_s_value_x1, ws.us_s_name_y1 + 2 * ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(spot_value);
            camo_value = new JLabel();
            camo_value.setHorizontalAlignment(SwingConstants.RIGHT);
            camo_value.setBounds(ws.us_s_value_x1, ws.us_s_name_y1 + 3 * ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(camo_value);
        }

        public void setValues(UnitType ut) {
            if (ut == null) {
                armor_value.setText("");
                agility_value.setText("");
                spot_value.setText("");
                camo_value.setText("");
            } else {
                armor_value.setText("" + ut.armor);
                agility_value.setText("" + ut.ag);
                spot_value.setText("" + ut.spot);
                camo_value.setText("" + ut.camo);
            }
        }

    }

    public static class Right extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private Gui gui;
        private WindowSize ws;
        private JLabel mvpts_name;
        private JLabel mvtype_name;
        private JLabel maint_name;
        private JLabel psydef_name;
        private JLabel mvpts_value;
        private JLabel mvtype_value;
        private JLabel maint_value;
        private JLabel psydef_value;

        public Right(Gui gui) {
            this.gui = gui;
            ws = Gui.getWindowSize();
            setLayout(null);
            setBackground(Color.BLACK);
            mvpts_name = new JLabel("MvPts");
            mvpts_name.setBounds(ws.us_s_name_x1, ws.us_s_name_y1, ws.us_s_name_w, ws.us_s_name_h);
            mvpts_name.setHorizontalAlignment(SwingConstants.LEFT);
            this.add(mvpts_name);

            mvtype_name = new JLabel("MvTp");
            mvtype_name.setBounds(ws.us_s_name_x1, ws.us_s_name_y1 + ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(mvtype_name);
            maint_name = new JLabel("Maint");
            maint_name.setBounds(ws.us_s_name_x1, ws.us_s_name_y1 + 2 * ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(maint_name);
            psydef_name = new JLabel("PsyDf");
            psydef_name.setBounds(ws.us_s_name_x1, ws.us_s_name_y1 + 3 * ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(psydef_name);
            mvpts_value = new JLabel();
            mvpts_value.setHorizontalAlignment(SwingConstants.RIGHT);
            mvpts_value.setBounds(ws.us_s_value_x1, ws.us_s_name_y1, ws.us_s_name_w, ws.us_s_name_h);
            this.add(mvpts_value);
            mvtype_value = new JLabel();
            mvtype_value.setHorizontalAlignment(SwingConstants.RIGHT);
            mvtype_value.setBounds(ws.us_s_value_x1, ws.us_s_name_y1 + ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(mvtype_value);
            maint_value = new JLabel();
            maint_value.setHorizontalAlignment(SwingConstants.RIGHT);
            maint_value.setBounds(ws.us_s_value_x1, ws.us_s_name_y1 + 2 * ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(maint_value);
            psydef_value = new JLabel();
            psydef_value.setHorizontalAlignment(SwingConstants.RIGHT);
            psydef_value.setBounds(ws.us_s_value_x1, ws.us_s_name_y1 + 3 * ws.us_s_name_h, ws.us_s_name_w, ws.us_s_name_h);
            this.add(psydef_value);
        }

        public void setValues(UnitType ut) {
            if (ut == null) {
                mvpts_value.setText("");
                mvtype_value.setText("");
                maint_value.setText("");
                psydef_value.setText("");
            } else {
                mvpts_value.setText("" + ut.move_pts);
                mvtype_value.setText(("" + ut.move_type).substring(0, 1));
                maint_value.setText("" + ut.crd_trn);
                psydef_value.setText("" + ut.psy_def);
            }
        }

    }

    public static class Attack extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private Gui gui;
        private WindowSize ws;
        private JLabel attack1_type;
        private JLabel attack2_type;
        private JLabel attack3_type;
        private JLabel attack4_type;
        private JLabel attack1_stat;
        private JLabel attack2_stat;
        private JLabel attack3_stat;
        private JLabel attack4_stat;

        public Attack(Gui gui) {
            this.gui = gui;
            ws = Gui.getWindowSize();
            setLayout(null);
            setBackground(Color.BLACK);
            attack1_type = new JLabel("Armor");
            attack1_type.setBounds(ws.us_a_name_x1, ws.us_a_name_y1, ws.us_a_name_w, ws.us_a_name_h);
            attack1_type.setHorizontalAlignment(SwingConstants.LEFT);
            this.add(attack1_type);

            attack2_type = new JLabel("Agility");
            attack2_type.setBounds(ws.us_a_name_x1, ws.us_a_name_y1 + ws.us_a_name_h, ws.us_a_name_w, ws.us_a_name_h);
            this.add(attack2_type);
            attack3_type = new JLabel("Spot");
            attack3_type.setBounds(ws.us_a_name_x1, ws.us_a_name_y1 + 2 * ws.us_a_name_h, ws.us_a_name_w, ws.us_a_name_h);
            this.add(attack3_type);
            attack4_type = new JLabel("Camo");
            attack4_type.setBounds(ws.us_a_name_x1, ws.us_a_name_y1 + 3 * ws.us_a_name_h, ws.us_a_name_w, ws.us_a_name_h);
            this.add(attack4_type);
            attack1_stat = new JLabel();
            attack1_stat.setHorizontalAlignment(SwingConstants.RIGHT);
            attack1_stat.setBounds(ws.us_a_value_x1, ws.us_a_name_y1, ws.us_a_name_w, ws.us_a_name_h);
            this.add(attack1_stat);
            attack2_stat = new JLabel();
            attack2_stat.setHorizontalAlignment(SwingConstants.RIGHT);
            attack2_stat.setBounds(ws.us_a_value_x1, ws.us_a_name_y1 + ws.us_a_name_h, ws.us_a_name_w, ws.us_a_name_h);
            this.add(attack2_stat);
            attack3_stat = new JLabel();
            attack3_stat.setHorizontalAlignment(SwingConstants.RIGHT);
            attack3_stat.setBounds(ws.us_a_value_x1, ws.us_a_name_y1 + 2 * ws.us_a_name_h, ws.us_a_name_w, ws.us_a_name_h);
            this.add(attack3_stat);
            attack4_stat = new JLabel();
            attack4_stat.setHorizontalAlignment(SwingConstants.RIGHT);
            attack4_stat.setBounds(ws.us_a_value_x1, ws.us_a_name_y1 + 3 * ws.us_a_name_h, ws.us_a_name_w, ws.us_a_name_h);
            this.add(attack4_stat);
        }

        private void setAttkStat(int field_no, String type, int acc, int str) {
            switch (field_no) {
                case 0:
                    attack1_type.setText(type);
                    attack1_stat.setText("" + acc + "/" + str);
                    break;

                case 1:
                    attack2_type.setText(type);
                    attack2_stat.setText("" + acc + "/" + str);
                    break;
                case 2:
                    attack3_type.setText(type);
                    attack3_stat.setText("" + acc + "/" + str);
                    break;
                case 3:
                    attack4_type.setText(type);
                    attack4_stat.setText("" + acc + "/" + str);
                    break;
                default:
                    System.out.println("Unit with over 4 attack types");
            }
        }

        public void setValues(UnitType ut) {
            attack1_type.setText("");
            attack1_stat.setText("");

            attack2_type.setText("");
            attack2_stat.setText("");

            attack3_type.setText("");
            attack3_stat.setText("");

            attack4_type.setText("");
            attack4_stat.setText("");

            if (ut == null) {
                return;
            }

            int field_no = 0;
            if (ut.water_str > 0) {
                setAttkStat(field_no++, "Water", ut.water_acc, ut.water_str);
            }
            if (ut.indirect_str > 0) {
                setAttkStat(field_no++, "Indirect", ut.indirect_acc, ut.indirect_str);
            }
            if (ut.air_str > 0) {
                setAttkStat(field_no++, "Air", ut.air_acc, ut.air_str);
            }
            if (ut.direct_str > 0) {
                setAttkStat(field_no++, "Direct", ut.direct_acc, ut.direct_str);
            }
            if (ut.close_str > 0) {
                setAttkStat(field_no++, "Close", ut.close_acc, ut.close_str);
            }
            if (ut.psy_str > 0) {
                setAttkStat(field_no++, "Psych", ut.psy_acc, ut.psy_str);
            }
            if (ut.ranged_sp_str > 0) {
                setAttkStat(field_no++, "Ranged Sp", ut.ranged_sp_acc, ut.ranged_sp_str);
            }
            if (ut.direct_sp_str > 0) {
                setAttkStat(field_no++, "Direct Sp", ut.direct_sp_acc, ut.direct_sp_str);
            }
            if (ut.close_sp_str > 0) {
                setAttkStat(field_no++, "Close Sp", ut.close_sp_acc, ut.close_sp_str);
            }

        }
    }

}
