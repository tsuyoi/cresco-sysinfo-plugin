package com.researchworx.cresco.plugins.sysinfo;

import com.researchworx.cresco.library.messaging.MsgEvent;
import com.researchworx.cresco.library.plugin.core.CPlugin;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PerfMonitor {
    private CPlugin plugin;
    private SysInfoBuilder builder;

    private Timer timer;
    private boolean running = false;

    public PerfMonitor(CPlugin plugin) {
        this.plugin = plugin;
        builder = new SysInfoBuilder();
    }

    public PerfMonitor start() {
        if (this.running) return this;
        Long interval = plugin.getConfig().getLongParam("perftimer", 5000L);

        MsgEvent initial = new MsgEvent(MsgEvent.Type.INFO, plugin.getRegion(), plugin.getAgent(), plugin.getPluginID(), "Performance Monitoring timer set to " + interval + " milliseconds.");
        initial.setParam("src_region", plugin.getRegion());
        initial.setParam("src_agent", plugin.getAgent());
        initial.setParam("src_plugin", plugin.getPluginID());
        initial.setParam("dst_region", plugin.getRegion());
        plugin.sendMsgEvent(initial);

        timer = new Timer();
        timer.scheduleAtFixedRate(new PerfMonitorTask(plugin), 500, interval);
        return this;
    }

    public PerfMonitor restart() {
        if (running) timer.cancel();
        running = false;
        return start();
    }

    public void stop() {
        timer.cancel();
        running = false;
    }

    private class PerfMonitorTask extends TimerTask {
        private CPlugin plugin;

        PerfMonitorTask(CPlugin plugin) {
            this.plugin = plugin;
        }

        public void run() {
            MsgEvent tick = new MsgEvent(MsgEvent.Type.INFO, plugin.getRegion(), plugin.getAgent(), plugin.getPluginID(), "Performance Monitoring tick.");
            tick.setParam("src_region", plugin.getRegion());
            tick.setParam("src_agent", plugin.getAgent());
            tick.setParam("src_plugin", plugin.getPluginID());
            tick.setParam("dst_region", plugin.getRegion());

            for(Map.Entry<String, String> entry : builder.getSysInfoMap().entrySet()) {
                tick.setParam(entry.getKey(), entry.getValue());
            }

            plugin.sendMsgEvent(tick);
        }
    }
}
