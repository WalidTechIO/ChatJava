package io.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Classe utilitaire qui permet de serialiser et deserialiser des objets Java.
 *
 * @author Eric Cariou
 */
public class Serialisation {

	/**
	 * Serialise un objet quelconque.
	 *
	 * @param obj l'objet à serialiser (qui doit implementer l'interface
	 *            <code>Serializable</code>)
	 * @return le tableau d'octets qui contient la serialisation de l'objet
	 * @throws IOException en cas d'erreur de serialisation
	 */
	public static byte[] serialiser(Serializable obj) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(bytes);
		output.writeObject(obj);
		return bytes.toByteArray();
	}

	/**
	 * Instancie un objet a partir de sa version serialisee.
	 *
	 * @param buffer le tableau d'octets qui contient la serialisation de l'objet
	 * @return un objet instancie a partir du contenu serialise
	 * @throws IOException            en cas d'erreur de deserialisation
	 * @throws ClassNotFoundException si la classe de l'objet n'est pas connue
	 */
	public static Object deserialiser(byte[] buffer) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(buffer));
		return objectInputStream.readObject();
	}

	/**
	 * Constructeur prive pour ne pas pouvoir instancier la classe.
	 */
	private Serialisation() {

	}
}
