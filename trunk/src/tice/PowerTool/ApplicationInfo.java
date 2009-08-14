package tice.PowerTool;

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
/*
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
    }
*/
}