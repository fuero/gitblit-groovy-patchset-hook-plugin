package at.jku.gitblit.plugin.patchset_hooks;

import com.gitblit.extensions.GitblitPlugin;

import ro.fortsoft.pf4j.PluginWrapper;
import ro.fortsoft.pf4j.Version;

public class Plugin extends GitblitPlugin {

	public Plugin(PluginWrapper wrapper) {
		super(wrapper);
	}
	
	@Override
	public void onInstall() {
		log.info("{} INSTALLED.", getWrapper().getPluginId());
	}

	@Override
	public void onUninstall() {
		log.info("{} UNINSTALLED.", getWrapper().getPluginId());
	}

	@Override
	public void onUpgrade(Version oldVersion) {
		log.info("{} UPGRADED from {}.", getWrapper().getPluginId(), oldVersion);
	}

}
