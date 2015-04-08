//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package fr.pasteque.client.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashcypher {

    /** Creates a new instance of Hashcypher */
    public Hashcypher() {
    }

    /** Compares password entered and password in the database.
     *
     * @param sPassword the password entered by user.
     * @param sHashPassword the password retrieved from the database.
     * @return true if password matches, false if not.
     */
    public boolean authenticate(String sPassword, String sHashPassword) {
        if (sHashPassword.startsWith("sha1:")) {
            return sHashPassword.equalsIgnoreCase(hashString(sPassword));
        } else if (sHashPassword.startsWith("plain:")) {
            return sHashPassword.equals("plain:" + sPassword);
        } else {
            return sHashPassword.equals(sPassword);
        }
    }

    /** Hashes the string received with SHA1
     *
     * @param sPassword the password entered by user.
     * @return the SHA-hashed string.
     */
    public static String hashString(String sPassword) {
        if (sPassword == null || sPassword.equals("")) {
            return "empty:";
        } else {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(sPassword.getBytes("UTF-8"));
                byte[] res = md.digest();
                String ret = "sha1:" + StringUtils.byte2hex(res);
                return "sha1:" + StringUtils.byte2hex(res);
            } catch (NoSuchAlgorithmException e) {
                return "plain:" + sPassword;
            } catch (UnsupportedEncodingException e) {
                return "plain:" + sPassword;
            }
        }
    }
}
