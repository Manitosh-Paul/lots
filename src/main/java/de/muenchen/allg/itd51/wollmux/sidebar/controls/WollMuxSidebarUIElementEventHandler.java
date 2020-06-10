package de.muenchen.allg.itd51.wollmux.sidebar.controls;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.muenchen.allg.itd51.wollmux.OpenExt;
import de.muenchen.allg.itd51.wollmux.core.dialog.UIElementConfig;
import de.muenchen.allg.itd51.wollmux.core.dialog.UIElementEventHandler;
import de.muenchen.allg.itd51.wollmux.core.dialog.controls.UIElement;
import de.muenchen.allg.itd51.wollmux.core.util.L;
import de.muenchen.allg.itd51.wollmux.dialog.InfoDialog;
import de.muenchen.allg.itd51.wollmux.event.handlers.OnAbout;
import de.muenchen.allg.itd51.wollmux.event.handlers.OnDumpInfo;
import de.muenchen.allg.itd51.wollmux.event.handlers.OnKill;
import de.muenchen.allg.itd51.wollmux.event.handlers.OnOpenDocument;
import de.muenchen.allg.itd51.wollmux.event.handlers.OnShowDialogAbsenderAuswaehlen;

/**
 * EventHandler für die WollMux-Sidebar. Der EventHandler behandelt alle WollMux-Aktionen,
 * die von Steuerelementen in der Sidebar ausgelöst werden, z.B. das Öffnen einer
 * WollMux-Vorlage.
 *
 */
public class WollMuxSidebarUIElementEventHandler implements UIElementEventHandler
{

  private static final Logger LOGGER = LoggerFactory
      .getLogger(WollMuxSidebarUIElementEventHandler.class);

  public WollMuxSidebarUIElementEventHandler()
  {
    super();
  }

  @Override
  public void processUiElementEvent(UIElement source, String eventType, Object[] args)
  {
    if (!eventType.equals("action")) return;

    String action = args[0].toString();
    if (action.equals("absenderAuswaehlen"))
    {
      new OnShowDialogAbsenderAuswaehlen().emit();
    }
    else if (action.equals("openDocument"))
    {
      String fragId = ((UIElementConfig) args[1]).getFragId();
      if (fragId.isEmpty())
      {
        LOGGER.error(L.m("ACTION \"%1\" erfordert mindestens ein Attribut FRAG_ID",
          action));
      } else
      {
        new OnOpenDocument(Arrays.asList(fragId), false).emit();
      }
    }
    else if (action.equals("openTemplate"))
    {
      String fragId = ((UIElementConfig) args[1]).getFragId();
      if (fragId.isEmpty())
      {
        LOGGER.error(L.m("ACTION \"%1\" erfordert mindestens ein Attribut FRAG_ID", action));
      } else
      {
        new OnOpenDocument(Arrays.asList(fragId), true).emit();
      }
    }
    else if (action.equals("open"))
    {
      InfoDialog.showInfoModal("Multiformulare werden nicht mehr unterstützt",
          "Multiformulare werden nicht mehr unterstützt. "
              + "Bitte kontaktieren Sie Ihren Administrator. "
              + "Sie müssen jedes Formular einzeln öffnen und ausfüllen.");
    }
    else if (action.equals("openExt"))
    {
      UIElementConfig conf = (UIElementConfig) args[1];
      executeOpenExt(conf.getExt(), conf.getUrl());
    }
    else if (action.equals("dumpInfo"))
    {
      new OnDumpInfo().emit();
    }
    else if (action.equals("abort"))
    {
      // abort();
    }
    else if (action.equals("kill"))
    {
      new OnKill().emit();
      // abort();
    }
    else if (action.equals("about"))
    {
      new OnAbout().emit();
    }
    else if (action.equals("options"))
    {
      // options();
    }
  }

  /**
   * Führt die gleichnamige ACTION aus.
   *
   * TESTED
   */
  private void executeOpenExt(String ext, String url)
  {
    try
    {
      final OpenExt openExt = OpenExt.getInstance(ext, url);

      try
      {
        openExt.storeIfNecessary();
      }
      catch (IOException x)
      {
        LOGGER.error("", x);
        showError(L.m("Fehler beim Download der Datei:\n%1", x.getMessage()));
        return;
      }

      Runnable launch = () ->
        openExt.launch((Exception x) ->
        {
          LOGGER.error("", x);
          showError(x.getMessage());
        });

      launch.run();
    }
    catch (Exception x)
    {
      LOGGER.error("", x);
      showError(x.getMessage());
    }
  }

  private void showError(String errorMsg)
  {
    InfoDialog.showInfoModal(L.m("Fehlerhafte Konfiguration"), L.m(
      "%1\nVerständigen Sie Ihre Systemadministration.", errorMsg));
  }
}
