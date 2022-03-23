/*
 * Copyright (c) 2003-2008, by Henrik Arro and Contributors
 *
 * This file is part of JSeq, a tool to automatically create
 * sequence diagrams by tracing program execution.
 *
 * See <http://jseq.sourceforge.net> for more information.
 *
 * JSeq is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * JSeq is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSeq. If not, see <http://www.gnu.org/licenses/>.
 */

package th.co.edge.jseq.argouml;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;

/**
 * A <code>UUID</code> is a utility class that is used to generate "globally"
 * unique identifiers.
 *
 * <p>
 * The algorithm uses the IP address of the machine running the code as part of
 * the identifier, so an external address needs to be available to make the
 * numbers really unique.
 */
public class UUID {
    private static String address = "";

    /**
     * Since this is a utility class containing only static methods, we hide the
     * only constructor.
     */
    private UUID() {
    }

    static {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            byte[] bytes = inetAddress.getAddress();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                Byte b = new Byte(bytes[i]);
                sb.append(b.longValue() + "-");
            }
            address = sb.toString();
        } catch (UnknownHostException e) {
            System.err.println(e);
        }
    }

    /**
     * Returns a globally unique identifier.
     *
     * @return a globally unique identifier
     */
    public static String getID() {
        UID uid = new UID();
        return address + uid;
    }
}
