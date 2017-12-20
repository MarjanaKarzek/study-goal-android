package com.studygoal.jisc.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Institution Model
 * <p>
 * Provides model "Institution" for Active Android.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
@Table(name = "Institutions")
public class Institution extends Model {

    @Column(name = "name")
    public String name;
    @Column(name = "url")
    public String url;
    @Column(name = "ukprn")
    public Integer ukprn;

    public Institution() {
        super();
    }

}
