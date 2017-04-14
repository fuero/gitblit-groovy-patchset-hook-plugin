package at.jku.gitblit.plugin.patchset_hooks;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitblit.IStoredSettings;
import com.gitblit.extensions.PatchsetHook;
import com.gitblit.manager.IGitblit;
import com.gitblit.models.TicketModel;
import com.gitblit.servlet.GitblitContext;
import com.gitblit.utils.StringUtils;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import ro.fortsoft.pf4j.Extension;

/**
 *
 * Groovy script patchset hook
 *
 * @author Robert Führicht
 *
 */
@Extension
public class GroovyPatchsetHook extends PatchsetHook {
	private static final Logger LOGGER = LoggerFactory.getLogger(GroovyPatchsetHook.class);
	final Logger log = LoggerFactory.getLogger(getClass());
	
	protected GroovyScriptEngine gse;
	protected final File groovyDir;
	protected String gitblitUrl;
	protected final IStoredSettings settings;
	protected final IGitblit gitblit;
	
	public GroovyPatchsetHook() { 
		this.gitblit = GitblitContext.getManager(IGitblit.class);
		this.settings = gitblit.getSettings();
		this.groovyDir = gitblit.getHooksFolder();
		try {
			// set Grape root
			File grapeRoot = gitblit.getGrapesFolder();
			grapeRoot.mkdirs();
			System.setProperty("grape.root", grapeRoot.getAbsolutePath());
			this.gse = new GroovyScriptEngine(groovyDir.getAbsolutePath());
		} catch (IOException e) {
		}
	}
	
	/**
	 * Runs the specified Groovy hook scripts.
	 *
	 * @param ticket
	 * @param scripts
	 */
	private void runGroovy(PatchsetEvent event, TicketModel ticket, Set<String> scripts) {
		if (scripts == null || scripts.size() == 0) {
			// no Groovy scripts to execute
			return;
		}

		Binding binding = new Binding();
		binding.setVariable("event", event);
		binding.setVariable("gitblit", gitblit);
		binding.setVariable("ticket", ticket);
		binding.setVariable("url", gitblitUrl);
		binding.setVariable("logger", LOGGER);
		for (String script : scripts) {
			if (StringUtils.isEmpty(script)) {
				continue;
			}
			// allow script to be specified without .groovy extension
			// this is easier to read in the settings
			File file = new File(groovyDir, script);
			if (!file.exists() && !script.toLowerCase().endsWith(".groovy")) {
				file = new File(groovyDir, script + ".groovy");
				if (file.exists()) {
					script = file.getName();
				}
			}
			try {
				Object result = gse.run(script, binding);
				if (result instanceof Boolean) {
					if (!((Boolean) result)) {
						LOGGER.error(MessageFormat.format(
								"Groovy script {0} has failed!  Hook scripts aborted.", script));
						break;
					}
				}
			} catch (Exception e) {
				LOGGER.error(
						MessageFormat.format("Failed to execute Groovy script {0}", script), e);
			}
		}
	}
	
    @Override
    public void onNewPatchset(TicketModel ticket) {
    	log.info(String.format("%s new patchset %s ticket-%d patchset %d",
    			getClass().getSimpleName(), ticket.repository, ticket.number,
    			ticket.getCurrentPatchset().number));
		// run Groovy hook scripts
		Set<String> scripts = new LinkedHashSet<String>();
		scripts.addAll(settings.getStrings("groovyPatchsetHook.onNewPatchset", ","));
		runGroovy(PatchsetEvent.New, ticket, scripts);
    }

    @Override
    public void onUpdatePatchset(TicketModel ticket) {
    	log.info(String.format("%s update patchset %s ticket-%d patchset %d-%d",
    			getClass().getSimpleName(), ticket.repository, ticket.number,
    			ticket.getCurrentPatchset().number,
    			ticket.getCurrentPatchset().rev));
		// run Groovy hook scripts
		Set<String> scripts = new LinkedHashSet<String>();
		scripts.addAll(settings.getStrings("groovyPatchsetHook.onUpdatePatchset", ","));
		runGroovy(PatchsetEvent.Update, ticket, scripts);
    }

    @Override
    public void onMergePatchset(TicketModel ticket) {
    	log.info(String.format("%s merge patchset %s ticket-%d SHA %s",
    			getClass().getSimpleName(), ticket.repository, ticket.number,
    			ticket.mergeSha));
		// run Groovy hook scripts
		Set<String> scripts = new LinkedHashSet<String>();
		scripts.addAll(settings.getStrings("groovyPatchsetHook.onMergePatchset", ","));
		runGroovy(PatchsetEvent.Merge, ticket, scripts);
    }
}