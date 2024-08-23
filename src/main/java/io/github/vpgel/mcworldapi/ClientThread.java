package io.github.vpgel.mcworldapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import static io.github.vpgel.mcworldapi.UsefulFunctions.*;

/**
 * <pre>
 * ClientThread behaves like so:
 * 
 * 1. Initialize input and output streams
 * 2. Execute the InputStreamReader.read() command. If it returns -1 - go to item 13
 * 3. Receive 2 bytes from the previous command
 * 4. Convert them into a number (0-65535) - it's the length of a request
 * 5. Receive length*2 bytes from the input
 * 6. Decode them into a UTF-16-BE string - it's a request
 * 7. Pass the request into a respond(String) command, which evaluates the command and returns a response
 * 8. Convert the response's length into 2 bytes (they can contain a number between 0-65535)
 * 9. Send the converted length to the output
 * 10. Encode the response in UTF-16-BE
 * 11. Send the response
 * 12. Repeat item 2
 * 13. Close the input, output streams and socket
 * </pre>
 */
public class ClientThread extends Thread {
    Socket socket;
    MinecraftServer server;
    BufferedReader in;
    BufferedWriter out;

    ClientThread(Socket socket, MinecraftServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        // Part 1. Initializing streams of receiving data and sending data ("in" and "out" respectively)
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-16BE"));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-16BE"));

        this.socket.setKeepAlive(true); // To check every 2 hours if the client crashed
    }

    /**
     * Handles the input and output streams (socket running) and the socket closing.
     */
    @Override
    public void run() {
        String request, response;
        try {
            int len = in.read();
            while (!this.isInterrupted() && len != -1) {
                char[] requestBuffer = new char[len];
                in.read(requestBuffer, 0, len);
                request = String.valueOf(requestBuffer);
                MCWAPI.logger.info(String.format("Received a request from %s: %s, with the size: %d characters", getAddress(socket), request, len));
    
                try {
                    response = respond(request);
                } catch (StringIndexOutOfBoundsException e) {
                    response = "Invalid syntax";
                }
                MCWAPI.logger.info(String.format("Sending a response to %s: %s, with the size: %d characters", getAddress(socket), response, response.length()));
                
                out.write(response.length());
                out.write(response);
                out.flush();
                
                //next length
                len = in.read();
            }
            MCWAPI.logger.info(String.format("Client %s stopped", getAddress(socket)));
            socket.close();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String respond(String request) throws StringIndexOutOfBoundsException {
        String command = request;
        String[] args = new String[0];
        int spaceIndex = request.indexOf(' ');
        if (spaceIndex != -1) {
            command = request.substring(0, request.indexOf(' '));
            args = request.substring(request.indexOf(" ") + 1, request.length() - 1).split(",");
        }

        return switch(command) {
            // get_block x,y,z,[dimension]
            case "get_block" -> {
                ResourceKey<Level> dimension = Level.OVERWORLD;
                if (args.length == 4) dimension = getDimension(args[3]);
                
                Block block = server.getLevel(dimension).getBlockState(new BlockPos(Integer.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[0]))).getBlock();
                yield block.getDescriptionId();
            }
            case "stop" -> {
                this.interrupt();
                MCWAPI.logger.info("INTERRUPT WAS CALLED");;
                yield "M'kay, aborting the connection";
            }
            default -> "Undefined command";
        };
    }
}
