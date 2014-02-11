/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author joulupunikki
 */
public class WindowSize {

    public final boolean is_double;
    /**
     * Main window width;
     */
    public int main_window_width = 640;
    /**
     * Main window height;
     */
    public int main_window_height = 480;
    public int space_map_x_pos = 124;
    public int space_map_y_pos = 29;
    public int space_map_width = 504;
    public int space_map_height = 435;
    public int space_map_square_width = 34;
    public int space_map_square_height = 34;
    public int starfld2_x_pos = 12;
    public int starfld2_y_pos = 9;
    public int planet_map_x_offset = 124;
    public int planet_map_y_offset = 32;
    public int planet_map_width = 504;
    public int planet_map_height = 400;
    public int planet_image_side = 32;
    public int unit_icon_size = 34;
    public int font_unit_icon_offset = 1;
    public int font_unit_icon_size = 9;
    public Font font_unit_icon;
    public int font_structure_name_size = 8;
    public Font font_structure_name_fg;
    public Font font_structure_name_bg;
    public int font_structure_name_gap = 1;
    public int stack_display_x_offset = 9;
    public int stack_display_y_offset = 181;
    public int stack_display_x2 = stack_display_x_offset + 3 * unit_icon_size;
    public int stack_display_y2 = stack_display_y_offset + 6 * unit_icon_size;
    public int stack_display_x3 = 9;
    public int stack_display_y3 = stack_display_y_offset + 6 * unit_icon_size;
    public int stack_display_x4 = stack_display_x_offset + 2 * unit_icon_size;
    public int stack_display_y4 = stack_display_y_offset + 7 * unit_icon_size;
    public int health_bar_width = 1;
    public int planet_map_hex_center_x_gap = 38;
    public int planet_map_hex_center_y_gap = 40;
    public int blip_side = 7;
    public int path_circle = 10;
    public int font_path_numbers_size = 20;
    public Font font_path_numbers;
    public double smxo = 1.9;
    public double smyo = 1.0;
    public int unit_panel_x_offset = 10;
    public int unit_panel_y_offset = 220;
    public int stack_window_exit_button_x = 570;
    public int stack_window_exit_button_y = 440;
    public int stack_window_exit_button_w = 40;
    public int stack_window_exit_button_h = 15;
    public int launch_button_x_offset = 95;
    public int launch_button_y_offset = 50;
    public int launch_button_width = 23;
    public int launch_button_height = 41;
    public int info_text_field_x = stack_display_x_offset;
    public int info_text_field_y = 100;
    public int info_text_field_y_space = 128;
    public int info_text_field_w = stack_display_x2 - stack_display_x_offset;
    public int info_text_field_h = 10;
    public Font font_default;
    public int font_default_size = 9;
    public int end_turn_button_w = 54;
    public int end_turn_button_h = 50;
    public int end_turn_button_x = 7;
    public int end_turn_button_y = 425;
    public int unit_order_buttons_x = 61;
    public int unit_order_buttons_y = 425;
    public int unit_order_buttons_w = 25;
    public int unit_order_buttons_h = 25;
    public int carry_symbol_x = 4;
    public int carry_symbol_y = 26;
    public int carry_symbol_w = 12;
    public int carry_symbol_h = 4;
    public Font font_abbrev;
    public int human_control_selection_x = 100;
    public int human_control_selection_y = 100;
    public int human_control_selection_w = 100;
    public int human_control_selection_h = 25;
    public int combat_window_stack_display_gap = 5;
    public int combat_window_stack_display_w = 5 * unit_icon_size + 6 * combat_window_stack_display_gap;
    public int combat_window_stack_display_h = 4 * unit_icon_size + 5 * combat_window_stack_display_gap;
    public int combat_window_stack_display_x = 15;
    public int combat_window_stack_display_x2 = main_window_width - combat_window_stack_display_w - combat_window_stack_display_x;
    public int combat_window_stack_display_y = 260;
    public int combat_window_fight_button_x = 280;
    public int combat_window_fight_button_y = 440;
    public int combat_window_fight_button_w = 80;
    public int combat_window_fight_button_h = 15;
    public int skull_offset = 3;
    public int skull_side = 40;
    public int unit_info_attack_type_x = 310;
    public int unit_info_attack_type_y = 118;
    public int unit_info_attack_stat_x = 370;
    public int unit_info_attack_stat_y = unit_info_attack_type_y;
    public int unit_info_attack_type_w = 60;
    public int unit_info_attack_type_h = 11;
    public int unit_info_attack_stat_w = 60;
    public int unit_info_attack_stat_h = unit_info_attack_type_h;
    public int unit_info_left_stat_x = 135;
    public int unit_info_left_stat_y = unit_info_attack_type_y;
    public int unit_info_left_stat_w = 30;
    public int unit_info_left_stat_h = unit_info_attack_type_h;
    public int unit_info_left_stat_x2 = 180;
    public int galactic_map_x_pos = 10;
    public int galactic_map_y_pos = 23;
    public int galactic_map_width = 100;
    public int galactic_map_height = 96;
    public int globe_map_x_pos = 3;
    public int globe_map_y_pos = 28;
    public int globe_map_width = 88;
    public int globe_map_height = 63;
    public int space_button_x_offset = 95;
    public int space_button_y_offset = 25;
    public int space_button_width = 23;
    public int space_button_height = 23;

    public WindowSize(boolean is_double) {

        this.is_double = is_double;
        if (is_double) {
            main_window_width *= 2;
            main_window_height *= 2;

            space_map_x_pos *= 2;
            space_map_y_pos *= 2;
            space_map_width *= 2;
            space_map_height *= 2;
            space_map_square_width *= 2;
            space_map_square_height *= 2;

            planet_map_x_offset *= 2;
            planet_map_y_offset *= 2;
            planet_map_width *= 2;
            planet_map_height *= 2;

            starfld2_x_pos *= 2;
            starfld2_y_pos *= 2;

            planet_image_side *= 2;

            unit_icon_size *= 2;
            font_unit_icon_size *= 2;
            font_unit_icon_offset *= 2;
            font_structure_name_size *= 2;

            stack_display_x_offset *= 2;
            stack_display_y_offset *= 2;

            health_bar_width *= 2;

            planet_map_hex_center_x_gap *= 2;
            planet_map_hex_center_y_gap *= 2;

            stack_display_x2 *= 2;
            stack_display_y2 *= 2;

            stack_display_x3 *= 2;
            stack_display_y3 *= 2;
            stack_display_x4 *= 2;
            stack_display_y4 *= 2;

            blip_side *= 2;

            font_structure_name_gap *= 2;

            path_circle *= 2;
            font_path_numbers_size *= 2;

            smxo *= 2;
            smyo *= 2;
            unit_panel_x_offset *= 2;
            unit_panel_y_offset *= 2;

            stack_window_exit_button_x *= 2;
            stack_window_exit_button_y *= 2;
            stack_window_exit_button_w *= 2;
            stack_window_exit_button_h *= 2;
            launch_button_x_offset *= 2;
            launch_button_y_offset *= 2;
            launch_button_width *= 2;
            launch_button_height *= 2;

            info_text_field_x *= 2;
            info_text_field_y *= 2;
            info_text_field_w *= 2;
            info_text_field_h *= 2;

            font_default_size *= 2;
            info_text_field_y_space *= 2;

            end_turn_button_w *= 2;
            end_turn_button_h *= 2;
            end_turn_button_x *= 2;
            end_turn_button_y *= 2;
            unit_order_buttons_x *= 2;
            unit_order_buttons_y *= 2;
            unit_order_buttons_w *= 2;
            unit_order_buttons_h *= 2;

            carry_symbol_x *= 2;
            carry_symbol_y *= 2;
            carry_symbol_w *= 2;
            carry_symbol_h *= 2;

            human_control_selection_x *= 2;
            human_control_selection_y *= 2;
            human_control_selection_w *= 2;
            human_control_selection_h *= 2;

            combat_window_stack_display_x *= 2;
            combat_window_stack_display_x2 *= 2;
            combat_window_stack_display_y *= 2;
            combat_window_stack_display_w *= 2;
            combat_window_stack_display_h *= 2;

            combat_window_fight_button_x *= 2;
            combat_window_fight_button_y *= 2;
            combat_window_fight_button_w *= 2;
            combat_window_fight_button_h *= 2;
            skull_offset *= 2;
            skull_side *= 2;

            unit_info_attack_type_x *= 2;
            unit_info_attack_type_y *= 2;
            unit_info_attack_stat_x *= 2;
            unit_info_attack_stat_y *= 2;
            unit_info_attack_type_w *= 2;
            unit_info_attack_type_h *= 2;
            unit_info_attack_stat_w *= 2;
            unit_info_attack_stat_h *= 2;
            unit_info_left_stat_x *= 2;
            unit_info_left_stat_y *= 2;
            unit_info_left_stat_w *= 2;
            unit_info_left_stat_h *= 2;
            unit_info_left_stat_x2 *= 2;

            galactic_map_x_pos *= 2;
            galactic_map_y_pos *= 2;
            galactic_map_width *= 2;
            galactic_map_height *= 2;

            globe_map_x_pos *= 2;
            globe_map_y_pos *= 2;
            globe_map_width *= 2;
            globe_map_height *= 2;

            space_button_x_offset *= 2;
            space_button_y_offset *= 2;
            space_button_width *= 2;
            space_button_height *= 2;

        }
        font_unit_icon = new Font("Arial", Font.PLAIN, font_unit_icon_size);

        font_structure_name_fg = new Font("Arial", Font.BOLD, font_structure_name_size);

        font_path_numbers = new Font("Arial", Font.BOLD, font_path_numbers_size);

        font_default = new Font("Arial", Font.BOLD, font_default_size);

        font_abbrev = new Font("Arial", Font.PLAIN, font_default_size);

    }
}
