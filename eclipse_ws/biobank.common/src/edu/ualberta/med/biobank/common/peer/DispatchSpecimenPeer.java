package edu.ualberta.med.biobank.common.peer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.ualberta.med.biobank.common.util.TypeReference;
import edu.ualberta.med.biobank.common.wrappers.Property;
import edu.ualberta.med.biobank.model.Comment;
import edu.ualberta.med.biobank.model.Dispatch;
import edu.ualberta.med.biobank.model.DispatchSpecimen;
import edu.ualberta.med.biobank.model.Specimen;
import edu.ualberta.med.biobank.model.type.DispatchSpecimenState;

public class DispatchSpecimenPeer {
	public static final Property<Integer, DispatchSpecimen> ID = Property.create(
		"id" //$NON-NLS-1$
		, DispatchSpecimen.class
		, new TypeReference<Integer>() {}
		, new Property.Accessor<Integer, DispatchSpecimen>() { private static final long serialVersionUID = 1L;
			@Override
			public Integer get(DispatchSpecimen model) {
				return model.getId();
			}
			@Override
			public void set(DispatchSpecimen model, Integer value) {
				model.setId(value);
			}
		});

	public static final Property<DispatchSpecimenState, DispatchSpecimen> STATE = Property.create(
		"state" //$NON-NLS-1$
		, DispatchSpecimen.class
		, new TypeReference<DispatchSpecimenState>() {}
		, new Property.Accessor<DispatchSpecimenState, DispatchSpecimen>() { private static final long serialVersionUID = 1L;
			@Override
			public DispatchSpecimenState get(DispatchSpecimen model) {
				return model.getState();
			}
			@Override
			public void set(DispatchSpecimen model, DispatchSpecimenState value) {
				model.setState(value);
			}
		});

	public static final Property<Collection<Comment>, DispatchSpecimen> COMMENTS = Property.create(
		"comments" //$NON-NLS-1$
		, DispatchSpecimen.class
		, new TypeReference<Collection<Comment>>() {}
		, new Property.Accessor<Collection<Comment>, DispatchSpecimen>() { private static final long serialVersionUID = 1L;
			@Override
			public Collection<Comment> get(DispatchSpecimen model) {
				return model.getComments();
			}
			@Override
			public void set(DispatchSpecimen model, Collection<Comment> value) {
				model.getComments().clear();
				model.getComments().addAll(value);
			}
		});

	public static final Property<Specimen, DispatchSpecimen> SPECIMEN = Property.create(
		"specimen" //$NON-NLS-1$
		, DispatchSpecimen.class
		, new TypeReference<Specimen>() {}
		, new Property.Accessor<Specimen, DispatchSpecimen>() { private static final long serialVersionUID = 1L;
			@Override
			public Specimen get(DispatchSpecimen model) {
				return model.getSpecimen();
			}
			@Override
			public void set(DispatchSpecimen model, Specimen value) {
				model.setSpecimen(value);
			}
		});

	public static final Property<Dispatch, DispatchSpecimen> DISPATCH = Property.create(
		"dispatch" //$NON-NLS-1$
		, DispatchSpecimen.class
		, new TypeReference<Dispatch>() {}
		, new Property.Accessor<Dispatch, DispatchSpecimen>() { private static final long serialVersionUID = 1L;
			@Override
			public Dispatch get(DispatchSpecimen model) {
				return model.getDispatch();
			}
			@Override
			public void set(DispatchSpecimen model, Dispatch value) {
				model.setDispatch(value);
			}
		});

   public static final List<Property<?, ? super DispatchSpecimen>> PROPERTIES;
   static {
      List<Property<?, ? super DispatchSpecimen>> aList = new ArrayList<Property<?, ? super DispatchSpecimen>>();
      aList.add(ID);
      aList.add(STATE);
      aList.add(COMMENTS);
      aList.add(SPECIMEN);
      aList.add(DISPATCH);
      PROPERTIES = Collections.unmodifiableList(aList);
   };
}
