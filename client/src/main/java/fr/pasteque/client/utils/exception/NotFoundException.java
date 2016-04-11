/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pasteque.client.utils.exception;

/**
 *
 * @author svirch_n
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("Could not found the element");
    }

    public NotFoundException(String className) {
        super("Could not found the element in " + className);
    }
    
    
}
