package se.despotify.domain.media;

import org.hibernate.annotations.CollectionOfElements;
import se.despotify.domain.Store;
import se.despotify.util.Hex;
import se.despotify.util.SpotifyURI;
import se.despotify.util.XMLElement;

import javax.persistence.Entity;
import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Track extends RestrictedMedia {

  private static final long serialVersionUID = 1L;

  private String title;
  @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
  private Artist artist;
  @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
  private Album album;
  private Integer year;
  private Integer discNumber;
  private Integer trackNumber;
  private Integer length;

  @CollectionOfElements
  private List<String> files;

  private String cover;
  private Float popularity;

  public Track() {
  }

  public Track(byte[] UUID) {
    this(Hex.toHex(UUID));
  }

  public Track(String hexUUID) {
    this.id = hexUUID;
  }

  public Track(String hexUUID, String title, Artist artist, Album album) {
    this(hexUUID);
    this.title = title;
    this.artist = artist;
    this.album = album;
  }

  public Track(byte[] UUID, String title, Artist artist, Album album) {
    this(UUID);
    this.title = title;
    this.artist = artist;
    this.album = album;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String getSpotifyURI() {
    return "spotify:track:" + SpotifyURI.toURI(getByteUUID());
  }

  @Override
  public String getHttpURL() {
    return "http://open.spotify.com/track/" + SpotifyURI.toURI(getByteUUID());
  }

  public Integer getDiscNumber() {
    return discNumber;
  }

  public void setDiscNumber(Integer discNumber) {
    this.discNumber = discNumber;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Artist getArtist() {
    return artist;
  }

  public void setArtist(Artist artist) {
    this.artist = artist;
  }

  public Album getAlbum() {
    return album;
  }

  public void setAlbum(Album album) {
    this.album = album;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Integer getTrackNumber() {
    return trackNumber;
  }

  public void setTrackNumber(Integer trackNumber) {
    this.trackNumber = trackNumber;
  }

  public Integer getLength() {
    return length;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  public List<String> getFiles() {
    return files;
  }

  public void setFiles(List<String> files) {
    this.files = files;
  }

  public String getCover() {
    return cover;
  }

  public void setCover(String cover) {
    this.cover = cover;
  }

  public Float getPopularity() {
    return popularity;
  }

  public void setPopularity(Float popularity) {
    this.popularity = popularity;
  }

  public static Track fromXMLElement(XMLElement trackElement, Store store) {

    Track track = store.getTrack(trackElement.getChildText("id"));

    /* Set title. */
    if (trackElement.hasChild("title")) {
      track.title = trackElement.getChildText("title");
    }

    /* Set artist. */
    if (trackElement.hasChild("artist-id") && trackElement.hasChild("artist")) {
      track.artist = store.getArtist(trackElement.getChildText("artist-id"));
      track.artist.setId(trackElement.getChildText("artist-id"));
      track.artist.setName(trackElement.getChildText("artist"));
    }

    /* Set album. */
    if (trackElement.hasChild("album-id")) {
      track.album = store.getAlbum(trackElement.getChildText("album-id"));
    }

    if (trackElement.hasChild("album")) {
      track.album.setName(trackElement.getChildText("album"));
    }

    /* Set year. */
    if (trackElement.hasChild("year")) {
      try {
        track.year = Integer.parseInt(trackElement.getChildText("year"));
      }
      catch (NumberFormatException e) {
        log.error("Could not read year from track", e);
        track.year = null;
      }
    }

    /* Set track number. */
    if (trackElement.hasChild("track-number")) {
      track.trackNumber = Integer.parseInt(trackElement.getChildText("track-number"));
    }

    /* Set length. */
    if (trackElement.hasChild("length")) {
      track.length = Integer.parseInt(trackElement.getChildText("length"));
    }

    /* Set files. */
    if (trackElement.hasChild("files")) {

      List<String> fileUUIDs = new ArrayList<String>();

      for (XMLElement fileElement : trackElement.getChild("files").getChildren()) {
        fileUUIDs.add(fileElement.getAttribute("id"));
      }

      track.setFiles(fileUUIDs);
    }

    /* Set cover. */
    // FIXED: now null if ""
    if (trackElement.hasChild("cover")) {
      String value = trackElement.getChildText("cover");
      if (!"".equals(value)) {
        track.cover = value;
      }
    }

    /* Set popularity. */
    if (trackElement.hasChild("popularity")) {
      track.popularity = Float.parseFloat(trackElement.getChildText("popularity"));
    }


    XMLElement restrictionsNode = trackElement.getChild("restrictions");
    if (restrictionsNode != null) {
      RestrictedMedia.fromXMLElement(restrictionsNode, track);
    }

    return track;

  }

  @Override
  public String toString() {
    return "Track{" +
        "id='" + id + '\'' +
        ", title='" + title + '\'' +
        ", artist=" + (artist == null ? null : artist.getId()) +
        ", album=" + (album == null ? null : album.getId()) +
        ", year=" + year +
        ", discNumber=" + discNumber +
        ", trackNumber=" + trackNumber +
        ", length=" + length +
        ", files=" + files +
        ", cover='" + cover + '\'' +
        ", popularity=" + popularity +
        '}';
  }

}
