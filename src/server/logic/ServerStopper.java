package server.logic;

/**
 * Thread permettant de stopper un {@link Server}.
 * (Utilise pour le mode loop et delay).
 */
class ServerStopper extends Thread {

    /**
     * Serveur a stopper.
     */
    private final Server server;

    /**
     * Delai en seconde avant de stopper le serveur.
     */
    private final int delay;

    /**
     * Constructeur.
     *
     * @param delay Delai en seconde avant de stopper le serveur.
     * @param server Serveur a stopper.
     */
    private ServerStopper(int delay, Server server) {
        this.delay = delay;
        this.server = server;
    }

    @Override
    public void run() {
        System.out.println("ServerStopper: Server will shutdown " + timeFormat(delay));
        try {
            Thread.sleep(delay* 1000L);
        } catch (InterruptedException ignored) {
            //Nobody have access to the thread object except this class and we don't call interrupt() method
        }
        server.stopServer();
        System.out.println("ServerStopper: Server stopped");
    }

    /**
     * Utilitaire de formatage de delai.
     * @param delay Delai a formatter.
     * @return Chaine formatter.
     */
    private String timeFormat(int delay) {
        if(delay < 4) return "now.";
        String res = "in ";

        int nbH = delay/3600;
        delay -= nbH*3600;
        int nbM = delay/60;
        delay -= nbM*60;

        if(nbH>0) res += nbH + " hour(s), ";
        if(nbM>0) res += nbM + " minute(s), ";
        if(delay>0) res += delay + " second(s).";
        else res = res.substring(0, res.length() - 2) + ".";

        return res;
    }

    /**
     * Methode d'invocation d'un ServerStopper.
     * @param delay Delai en seconde avant de stopper le serveur.
     * @param server Serveur a stopper.
     */
    static void invoke(int delay, Server server) {
        new ServerStopper(delay, server).start();
    }
}