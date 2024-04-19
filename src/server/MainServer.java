package server;

import server.logic.Server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * CLI Serveur.
 */
public class MainServer {

	/**
	 * Utilisation de la CLI.
	 */
    private static final String usage = """
                Usage: program port [-b address] [-p port] [-d delay] [-l delay] [-m nio]
                
                port: Valid port for MutliCast group. (required)
                -b: Valid address for MultiCast group. (default: 230.0.0.0)
                -p: Valid port for TCP Socket. (default: random)
                -d: Stop server after delay seconds.
                -l: Reboot server every delay seconds.
                -m nio: Server will be in NIO mode.""";

    /**
     * Main.
     * @param args Arguments de lancement CLI.
     */
    public static void main(String[] args) {

        /* Verifie que le nb d'arguments est impair (1+(couple d'arguments*x) = nb impair) */
        if(args.length%2 == 0 || args.length > 9) {
            System.out.println(usage);
            System.exit(0);
        }

        String mcAdr = "230.0.0.0"; /* Default MultiCast Group address */
        int mcPort = -1; /* MutliCast Group port */
        int tcpPort = 0; /* Default TCP Socket port (0 will choose random available port) */
        int delay = -1;
        boolean nioMode = false;
        boolean loop = false;

        //Parse les arguments et les verifies
        try {
            mcPort = Integer.parseInt(args[0]); //args[0] = Bind port de groupe mutlicast
            if (mcPort < 1 || mcPort > 65535) throw new IllegalArgumentException(mcPort + " is not a valid port.");
            for (int i = 1; i < args.length - 1; i += 2) {

                if (args[i].equals("-b")) { //-b = Bind address de groupe multicast
                    mcAdr = args[i + 1];
                    if (!InetAddress.getByName(mcAdr).isMulticastAddress()) throw new IllegalArgumentException(mcAdr + " is not a valid multicast address");
                    continue;
                }

                if (args[i].equals("-p")) { //-p = Bind port d'ecoute socket TCP du chat
                    tcpPort = Integer.parseInt(args[i + 1]);
                    if (tcpPort < 0 || tcpPort > 65535) throw new IllegalArgumentException(tcpPort + " is not a valid port.");
                    continue;
                }

                if(args[i].equals("-d") || args[i].equals("-l")) { //-d|-l = Delai de vie du server (-l = loop)
                    delay = Integer.parseInt(args[i+1]);
                    if(delay < 0) throw new IllegalArgumentException(delay + " is not a valid delay.");
                    if(args[i].equals("-l")) loop = true;
                    continue;
                }

                if(args[i].equals("-m") && args[i+1].equals("nio")) { //-m nio = Serveur utilise java.nio
                    nioMode = true;
                    continue;
                }

                throw new IllegalArgumentException("Flag inconnu: " + args[i] + " " + args[i + 1]); //Flag inconnu
            }

        } catch (NumberFormatException e) {
            String invalidNumber = e.getMessage().split(":")[1].trim().replace("\"", "");
           usageError(invalidNumber + " is not a valid number.");
        } catch (UnknownHostException e) {
            usageError(mcAdr + " is not a valid multicast address");
        } catch (IllegalArgumentException e) {
            usageError(e.getMessage());
        }

        //Ajout d'un message de fin et affichage message de debut
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Good Bye !")));
        System.out.println("Server starting...");

        Server server = null;

        do {
            //Instantie et demarre le serveur
            try {
                server = Server.invoke(tcpPort, new InetSocketAddress(mcAdr, mcPort), delay, nioMode);
            } catch(Exception e) {
                System.err.println("Can't initiate server cause : " + e.getMessage());
                System.exit(1);
            }

            server.run();
        } while (loop);

    }

    /**
     * Affiche l'usage et l'erreur d'usage
     * @param msg Erreur a afficher.
     */
    static void usageError(String msg) {
        System.out.println(usage);
        System.err.println("\nErreur: " + msg);
        System.exit(0);
    }

    /**
     * Noon utilise.
     */
    private MainServer() {}

}