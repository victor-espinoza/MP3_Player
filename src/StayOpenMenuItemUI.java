
import javax.swing.JComponent;
import javax.swing.MenuSelectionManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Victor Espinoza
 */
public class StayOpenMenuItemUI extends BasicMenuItemUI{
   @Override
   protected void doClick(MenuSelectionManager msm) {
      menuItem.doClick(0);
   }

   public static ComponentUI createUI(JComponent c) {
      return new StayOpenMenuItemUI();
   }
}
