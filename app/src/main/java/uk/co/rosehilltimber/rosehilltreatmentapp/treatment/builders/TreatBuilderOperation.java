package uk.co.rosehilltimber.rosehilltreatmentapp.treatment.builders;

public enum TreatBuilderOperation
{

    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete");

    private String mName;

    TreatBuilderOperation(final String mName)
    {
        this.mName = mName;
    }

    public String getName()
    {
        return mName;
    }
}
