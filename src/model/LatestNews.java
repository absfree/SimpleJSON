package model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/5/17.
 */
public class LatestNews {
    private String date;
    private List<TopStory> top_stories;
    private List<Story> stories;


    public void setTopStories(List<TopStory> top_stories) {
        this.top_stories = top_stories;
    }

    public void setStories(List<Story> stories) {
        this.stories = stories;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<TopStory> getTop_stories() {
        return top_stories;
    }

    public List<Story> getStories() {
        return stories;
    }

    public String getDate() {
        return date;
    }

    public static class TopStory {
        private String image;
        private int type;
        private int id;
        private String title;

        public void setId(int id) {
            this.id = id;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getImage() {
            return image;
        }

        public int getType() {
            return type;
        }

        public String toString() {
            return "id =  " + id + ", title = " + title + ", image = " + image + ", type = " + type + "\n";
        }

    }

    public static class Story implements Serializable {
        private List<String> images;
        private int type;
        private int id;
        private String title;

        public List<String> getImages() {
            return images;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String toString() {
            return "id =  " + id + ", title = " + title + ", images = " + images.get(0) + ", type = " + type + "\n";
        }

    }


}

