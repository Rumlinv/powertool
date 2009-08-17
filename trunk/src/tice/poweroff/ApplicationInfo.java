package tice.poweroff;

class ApplicationInfo {

    public String [] mTitlelist = null;
    public String [] mPackagelist = null;
    public String [] mNamelist = null;
    public boolean [] mChecklist = null;
    
    public ApplicationInfo(int count){
        mTitlelist = new String[count];
        mPackagelist = new String[count];
        mNamelist = new String[count];
        mChecklist = new boolean[count];
    }
}