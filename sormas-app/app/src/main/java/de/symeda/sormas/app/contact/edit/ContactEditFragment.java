package de.symeda.sormas.app.contact.edit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.List;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.contact.ContactClassification;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.contact.ContactProximity;
import de.symeda.sormas.api.contact.ContactRelation;
import de.symeda.sormas.app.BaseEditActivityFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.contact.Contact;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.caze.edit.CaseEditActivity;
import de.symeda.sormas.app.caze.edit.CaseNewActivity;
import de.symeda.sormas.app.component.Item;
import de.symeda.sormas.app.component.OnLinkClickListener;
import de.symeda.sormas.app.component.TeboSpinner;
import de.symeda.sormas.app.component.VisualState;
import de.symeda.sormas.app.core.BoolResult;
import de.symeda.sormas.app.core.IActivityCommunicator;
import de.symeda.sormas.app.core.async.IJobDefinition;
import de.symeda.sormas.app.core.async.ITaskExecutor;
import de.symeda.sormas.app.core.async.ITaskResultCallback;
import de.symeda.sormas.app.core.async.ITaskResultHolderIterator;
import de.symeda.sormas.app.core.async.TaskExecutorFor;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.databinding.FragmentContactEditLayoutBinding;
import de.symeda.sormas.app.shared.CaseFormNavigationCapsule;
import de.symeda.sormas.app.shared.ContactFormNavigationCapsule;
import de.symeda.sormas.app.util.DataUtils;

/**
 * Created by Orson on 12/02/2018.
 * <p>
 * www.technologyboard.org
 * sampson.orson@gmail.com
 * sampson.orson@technologyboard.org
 */

public class ContactEditFragment extends BaseEditActivityFragment<FragmentContactEditLayoutBinding, Contact, Contact> {

    private AsyncTask onResumeTask;
    private String recordUuid = null;
    private ContactClassification pageStatus = null;
    private Contact record;
    private Case associatedCase;
    private View.OnClickListener createCaseCallback;
    private View.OnClickListener openCaseCallback;
    private OnLinkClickListener openCaseLinkCallback;

    private List<Item> relationshipList;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        SavePageStatusState(outState, pageStatus);
        SaveRecordUuidState(outState, recordUuid);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = (savedInstanceState != null)? savedInstanceState : getArguments();

        recordUuid = getRecordUuidArg(arguments);
        pageStatus = (ContactClassification) getPageStatusArg(arguments);
    }

    @Override
    protected String getSubHeadingTitle() {
        return null;
    }

    @Override
    public Contact getPrimaryData() {
        return record;
    }

    @Override
    public boolean onBeforeLayoutBinding(Bundle savedInstanceState, TaskResultHolder resultHolder, BoolResult resultStatus, boolean executionComplete) {
        if (!executionComplete) {
            Case _associatedCase = null;
            Contact contact = getActivityRootData();

            if (contact != null) {
                if (contact.isUnreadOrChildUnread())
                    DatabaseHelper.getContactDao().markAsRead(contact);

                _associatedCase = findAssociatedCase(contact.getPerson(), contact.getCaze().getDisease());
            }

            resultHolder.forItem().add(contact);
            resultHolder.forItem().add(_associatedCase);

            resultHolder.forOther().add(DataUtils.getEnumItems(ContactRelation.class, false));
        } else {
            ITaskResultHolderIterator itemIterator = resultHolder.forItem().iterator();
            ITaskResultHolderIterator otherIterator = resultHolder.forOther().iterator();

            //Item Data
            if (itemIterator.hasNext())
                record = itemIterator.next();

            if (record == null)
                getActivity().finish();

            if (itemIterator.hasNext())
                associatedCase = itemIterator.next();

            if (otherIterator.hasNext())
                relationshipList =  otherIterator.next();

            setupCallback();
        }

        return true;
    }

    @Override
    public void onLayoutBinding(FragmentContactEditLayoutBinding contentBinding) {

        //FieldHelper.initSpinnerField(binding.contactContactClassification, ContactClassification.class);
        //FieldHelper.initSpinnerField(binding.contactContactStatus, ContactStatus.class);
        //FieldHelper.initSpinnerField(binding.contactRelationToCase, ContactRelation.class);

        updateBottonPanel();


        setVisibilityByDisease(ContactDto.class, record.getCaze().getDisease(), contentBinding.mainContent);

        //contentBinding.contactLastContactDate.makeFieldSoftRequired();
        //contentBinding.contactContactProximity.makeFieldSoftRequired();
        //contentBinding.contactRelationToCase.makeFieldSoftRequired();

        contentBinding.setData(record);
        contentBinding.setContactProximityClass(ContactProximity.class);
        contentBinding.setCreateCaseCallback(createCaseCallback);
        contentBinding.setOpenCaseLinkCallback(openCaseLinkCallback);
        contentBinding.setOpenCaseCallback(openCaseCallback);
    }

    @Override
    public void onAfterLayoutBinding(FragmentContactEditLayoutBinding contentBinding) {
        contentBinding.spnContactRelationship.initialize(new TeboSpinner.ISpinnerInitSimpleConfig() {
            @Override
            public Object getSelectedValue() {
                return null;
            }

            @Override
            public List<Item> getDataSource(Object parentValue) {
                return (relationshipList.size() > 0) ? DataUtils.addEmptyItem(relationshipList)
                        : relationshipList;
            }

            @Override
            public VisualState getInitVisualState() {
                return null;
            }
        });


        contentBinding.dtpDateOfLastContact.initialize(getFragmentManager());
    }

    @Override
    protected void updateUI(FragmentContactEditLayoutBinding contentBinding, Contact contact) {

    }

    @Override
    public void onPageResume(FragmentContactEditLayoutBinding contentBinding, boolean hasBeforeLayoutBindingAsyncReturn) {
        if (!hasBeforeLayoutBindingAsyncReturn)
            return;

        try {
            ITaskExecutor executor = TaskExecutorFor.job(new IJobDefinition() {
                @Override
                public void preExecute(BoolResult resultStatus, TaskResultHolder resultHolder) {
                    //getActivityCommunicator().showPreloader();
                    //getActivityCommunicator().hideFragmentView();
                }

                @Override
                public void execute(BoolResult resultStatus, TaskResultHolder resultHolder) {
                    if (recordUuid != null && !recordUuid.isEmpty()) {
                        Contact contact = DatabaseHelper.getContactDao().queryUuid(recordUuid);

                        if (contact != null && contact.isUnreadOrChildUnread())
                            DatabaseHelper.getContactDao().markAsRead(contact);

                        resultHolder.forItem().add(contact);
                    } else {
                        resultHolder.forItem().add(null);
                    }
                }
            });
            onResumeTask = executor.execute(new ITaskResultCallback() {
                @Override
                public void taskResult(BoolResult resultStatus, TaskResultHolder resultHolder) {
                    //getActivityCommunicator().hidePreloader();
                    //getActivityCommunicator().showFragmentView();

                    if (resultHolder == null){
                        return;
                    }

                    ITaskResultHolderIterator itemIterator = resultHolder.forItem().iterator();

                    if (itemIterator.hasNext())
                        record = itemIterator.next();

                    if (record != null)
                        requestLayoutRebind();
                    else {
                        getActivity().finish();
                    }
                }
            });
        } catch (Exception ex) {
            //getActivityCommunicator().hidePreloader();
            //getActivityCommunicator().showFragmentView();
        }

    }

    @Override
    public int getEditLayout() {
        return R.layout.fragment_contact_edit_layout;
    }

    @Override
    public boolean includeFabNonOverlapPadding() {
        return false;
    }

    @Override
    public boolean showSaveAction() {
        return true;
    }

    @Override
    public boolean showAddAction() {
        return false;
    }

    private Case findAssociatedCase(Person person, Disease disease) {
        if(person == null || disease == null) {
            return null;
        }

        Case caze = DatabaseHelper.getCaseDao().getByPersonAndDisease(person, disease);
        if (caze != null) {
            return caze;
        } else {
            return null;
        }
    }

    private void setupCallback() {
        createCaseCallback = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaseFormNavigationCapsule dataCapsule = (CaseFormNavigationCapsule)new CaseFormNavigationCapsule(getContext())
                        .setContactUuid(recordUuid).setPersonUuid(record.getPerson().getUuid());
                CaseNewActivity.goToActivity(getContext(), dataCapsule);
            }
        };
        openCaseCallback = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCase();
            }
        };

        openCaseLinkCallback = new OnLinkClickListener() {
            @Override
            public void onClick(View v, Object item) {
                openCase();
            }
        };
    }

    private void openCase() {
        if (associatedCase != null) {
            CaseFormNavigationCapsule dataCapsule = new CaseFormNavigationCapsule(getContext(),
                    associatedCase.getUuid()).setReadPageStatus(associatedCase.getCaseClassification());
            CaseEditActivity.goToActivity(getActivity(), dataCapsule);
        }
    }

    private void updateBottonPanel() {
        if (associatedCase == null) {
            getContentBinding().btnOpenCase.setVisibility(View.GONE);
        } else {
            getContentBinding().btnCreateCase.setVisibility(View.GONE);
        }

        if (getContentBinding().btnOpenCase.getVisibility() == View.GONE && getContentBinding().btnCreateCase.getVisibility() == View.GONE) {
            getContentBinding().contactPageBottomCtrlPanel.setVisibility(View.GONE);
        }
    }

    public static ContactEditFragment newInstance(IActivityCommunicator activityCommunicator, ContactFormNavigationCapsule capsule, Contact activityRootData)
            throws java.lang.InstantiationException, IllegalAccessException {
        return newInstance(activityCommunicator, ContactEditFragment.class, capsule, activityRootData);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (onResumeTask != null && !onResumeTask.isCancelled())
            onResumeTask.cancel(true);
    }
}