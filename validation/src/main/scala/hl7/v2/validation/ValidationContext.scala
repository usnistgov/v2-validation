package hl7.v2.validation

import java.io.InputStream

import hl7.v2.profile.Profile
import hl7.v2.validation.coconstraints.{CoConstraintValidationContext, EmptyCoConstraintValidationContext}
import hl7.v2.validation.content.{DefaultConformanceContext, EmptyConformanceContext}
import hl7.v2.validation.slicing.{EmptyProfileSlicingContext, ProfileSlicingContext}
import hl7.v2.validation.vs.{EmptyValueSetLibrary, EmptyValueSetSpecification, ValueSetLibraryImpl}
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.{Failure, Success}


case class ValidationContext(
  profile: Profile,
  valueSetLibrary: vs.ValueSetLibrary = EmptyValueSetLibrary.getInstance(),
  conformanceContext: content.ConformanceContext = EmptyConformanceContext,
  vsSpecification : vs.ValueSetSpecification = EmptyValueSetSpecification,
  coConstraintValidationContext: CoConstraintValidationContext = EmptyCoConstraintValidationContext,
  slicingContext: ProfileSlicingContext = EmptyProfileSlicingContext,
  featureFlags: FeatureFlags = FeatureFlags()
) {

}

class ValidationContextBuilder(profileInputStream: InputStream) {
  var profile: Option[Profile] = useProfile(profileInputStream)
  var valueSetLibrary: vs.ValueSetLibrary = EmptyValueSetLibrary.getInstance()
  var conformanceContext: content.ConformanceContext = EmptyConformanceContext
  var vsSpecification : vs.ValueSetSpecification = EmptyValueSetSpecification
  var coConstraintValidationContext: CoConstraintValidationContext = EmptyCoConstraintValidationContext
  var slicingContext: ProfileSlicingContext = EmptyProfileSlicingContext
  var featureFlags: FeatureFlags = FeatureFlags()

  private def useProfile(profileInputStream: InputStream) = {
    hl7.v2.profile.XMLDeserializer.deserialize(profileInputStream) match {
      case Success(value) => Some(value)
      case Failure(exception) => throw exception
    }
  }

  def useValueSetLibrary(libraryInputStream: InputStream): ValidationContextBuilder = {
    ValueSetLibraryImpl(libraryInputStream) match {
      case Success(vsLibrary) => valueSetLibrary = vsLibrary
      case Failure(exception) => throw exception
    }
    this
  }

  def useConformanceContext(conformanceContexts: List[InputStream]): ValidationContextBuilder = {
    DefaultConformanceContext.apply(conformanceContexts: _*) match {
      case Success(cfc) => conformanceContext = cfc
      case Failure(exception) => throw exception
    }
    this
  }

  def useConformanceContext(conformanceContexts: java.util.List[InputStream]): ValidationContextBuilder = {
    DefaultConformanceContext.apply(conformanceContexts.asScala.toList:_*) match {
      case Success(cfc) => conformanceContext = cfc
      case Failure(exception) => throw exception
    }
    this
  }

  def useVsBindings(vsBindings: InputStream): ValidationContextBuilder = {
    hl7.v2.validation.vs.DefaultValueSetSpecification(vsBindings) match {
      case Success(vsBind) => vsSpecification = vsBind
      case Failure(exception) => throw exception
    }
    this
  }

  def useCoConstraintsContext(cc: InputStream): ValidationContextBuilder = {
    hl7.v2.validation.coconstraints.DefaultCoConstraintValidationContext(cc) match {
      case Success(ccc) => coConstraintValidationContext = ccc
      case Failure(exception) => throw exception
    }
    this
  }

  def useSlicingContext(slicing: InputStream): ValidationContextBuilder = {
    profile match {
      case None => throw new Exception("No profile defined")
      case Some(p) => hl7.v2.validation.slicing.DefaultProfileSlicingContext(slicing, p) match {
        case Success(slicingC) => slicingContext = slicingC
        case Failure(exception) => throw exception
      }
    }
    this
  }

  def useFeatureFlags(ff: FeatureFlags): ValidationContextBuilder = {
    featureFlags = ff
    this
  }

  def setFFLegacy0396(value: Boolean): ValidationContextBuilder = {
    featureFlags = featureFlags.copy(legacy0396 = value)
    this
  }

  def getValidationContext: ValidationContext = {
    profile match {
      case None => throw new Exception("No profile defined")
      case Some(p) => ValidationContext(
        p,
        valueSetLibrary,
        conformanceContext,
        vsSpecification,
        coConstraintValidationContext,
        slicingContext,
        featureFlags
      )
    }
  }
}