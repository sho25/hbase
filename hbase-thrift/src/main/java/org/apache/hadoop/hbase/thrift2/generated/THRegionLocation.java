begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift Compiler (0.12.0)  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  *  @generated  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift2
operator|.
name|generated
package|;
end_package

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"cast"
block|,
literal|"rawtypes"
block|,
literal|"serial"
block|,
literal|"unchecked"
block|,
literal|"unused"
block|}
argument_list|)
annotation|@
name|javax
operator|.
name|annotation
operator|.
name|Generated
argument_list|(
name|value
operator|=
literal|"Autogenerated by Thrift Compiler (0.12.0)"
argument_list|,
name|date
operator|=
literal|"2020-01-22"
argument_list|)
specifier|public
class|class
name|THRegionLocation
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TBase
argument_list|<
name|THRegionLocation
argument_list|,
name|THRegionLocation
operator|.
name|_Fields
argument_list|>
implements|,
name|java
operator|.
name|io
operator|.
name|Serializable
implements|,
name|Cloneable
implements|,
name|Comparable
argument_list|<
name|THRegionLocation
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TStruct
name|STRUCT_DESC
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TStruct
argument_list|(
literal|"THRegionLocation"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
name|SERVER_NAME_FIELD_DESC
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
argument_list|(
literal|"serverName"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TType
operator|.
name|STRUCT
argument_list|,
operator|(
name|short
operator|)
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
name|REGION_INFO_FIELD_DESC
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
argument_list|(
literal|"regionInfo"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TType
operator|.
name|STRUCT
argument_list|,
operator|(
name|short
operator|)
literal|2
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|SchemeFactory
name|STANDARD_SCHEME_FACTORY
init|=
operator|new
name|THRegionLocationStandardSchemeFactory
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|SchemeFactory
name|TUPLE_SCHEME_FACTORY
init|=
operator|new
name|THRegionLocationTupleSchemeFactory
argument_list|()
decl_stmt|;
specifier|public
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
name|TServerName
name|serverName
decl_stmt|;
comment|// required
specifier|public
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
name|THRegionInfo
name|regionInfo
decl_stmt|;
comment|// required
comment|/** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
specifier|public
enum|enum
name|_Fields
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TFieldIdEnum
block|{
name|SERVER_NAME
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|,
literal|"serverName"
argument_list|)
block|,
name|REGION_INFO
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|,
literal|"regionInfo"
argument_list|)
block|;
specifier|private
specifier|static
specifier|final
name|java
operator|.
name|util
operator|.
name|Map
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|,
name|_Fields
argument_list|>
name|byName
init|=
operator|new
name|java
operator|.
name|util
operator|.
name|HashMap
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|,
name|_Fields
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
for|for
control|(
name|_Fields
name|field
range|:
name|java
operator|.
name|util
operator|.
name|EnumSet
operator|.
name|allOf
argument_list|(
name|_Fields
operator|.
name|class
argument_list|)
control|)
block|{
name|byName
operator|.
name|put
argument_list|(
name|field
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Find the _Fields constant that matches fieldId, or null if its not found.      */
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
specifier|public
specifier|static
name|_Fields
name|findByThriftId
parameter_list|(
name|int
name|fieldId
parameter_list|)
block|{
switch|switch
condition|(
name|fieldId
condition|)
block|{
case|case
literal|1
case|:
comment|// SERVER_NAME
return|return
name|SERVER_NAME
return|;
case|case
literal|2
case|:
comment|// REGION_INFO
return|return
name|REGION_INFO
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
comment|/**      * Find the _Fields constant that matches fieldId, throwing an exception      * if it is not found.      */
specifier|public
specifier|static
name|_Fields
name|findByThriftIdOrThrow
parameter_list|(
name|int
name|fieldId
parameter_list|)
block|{
name|_Fields
name|fields
init|=
name|findByThriftId
argument_list|(
name|fieldId
argument_list|)
decl_stmt|;
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
throw|throw
operator|new
name|java
operator|.
name|lang
operator|.
name|IllegalArgumentException
argument_list|(
literal|"Field "
operator|+
name|fieldId
operator|+
literal|" doesn't exist!"
argument_list|)
throw|;
return|return
name|fields
return|;
block|}
comment|/**      * Find the _Fields constant that matches name, or null if its not found.      */
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
specifier|public
specifier|static
name|_Fields
name|findByName
parameter_list|(
name|java
operator|.
name|lang
operator|.
name|String
name|name
parameter_list|)
block|{
return|return
name|byName
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|short
name|_thriftId
decl_stmt|;
specifier|private
specifier|final
name|java
operator|.
name|lang
operator|.
name|String
name|_fieldName
decl_stmt|;
name|_Fields
parameter_list|(
name|short
name|thriftId
parameter_list|,
name|java
operator|.
name|lang
operator|.
name|String
name|fieldName
parameter_list|)
block|{
name|_thriftId
operator|=
name|thriftId
expr_stmt|;
name|_fieldName
operator|=
name|fieldName
expr_stmt|;
block|}
specifier|public
name|short
name|getThriftFieldId
parameter_list|()
block|{
return|return
name|_thriftId
return|;
block|}
specifier|public
name|java
operator|.
name|lang
operator|.
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|_fieldName
return|;
block|}
block|}
comment|// isset id assignments
specifier|public
specifier|static
specifier|final
name|java
operator|.
name|util
operator|.
name|Map
argument_list|<
name|_Fields
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
argument_list|>
name|metaDataMap
decl_stmt|;
static|static
block|{
name|java
operator|.
name|util
operator|.
name|Map
argument_list|<
name|_Fields
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
argument_list|>
name|tmpMap
init|=
operator|new
name|java
operator|.
name|util
operator|.
name|EnumMap
argument_list|<
name|_Fields
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
argument_list|>
argument_list|(
name|_Fields
operator|.
name|class
argument_list|)
decl_stmt|;
name|tmpMap
operator|.
name|put
argument_list|(
name|_Fields
operator|.
name|SERVER_NAME
argument_list|,
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
argument_list|(
literal|"serverName"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TFieldRequirementType
operator|.
name|REQUIRED
argument_list|,
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|StructMetaData
argument_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TType
operator|.
name|STRUCT
argument_list|,
name|TServerName
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tmpMap
operator|.
name|put
argument_list|(
name|_Fields
operator|.
name|REGION_INFO
argument_list|,
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
argument_list|(
literal|"regionInfo"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TFieldRequirementType
operator|.
name|REQUIRED
argument_list|,
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|StructMetaData
argument_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TType
operator|.
name|STRUCT
argument_list|,
name|THRegionInfo
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|metaDataMap
operator|=
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|tmpMap
argument_list|)
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|meta_data
operator|.
name|FieldMetaData
operator|.
name|addStructMetaDataMap
argument_list|(
name|THRegionLocation
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|THRegionLocation
parameter_list|()
block|{   }
specifier|public
name|THRegionLocation
parameter_list|(
name|TServerName
name|serverName
parameter_list|,
name|THRegionInfo
name|regionInfo
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
block|}
comment|/**    * Performs a deep copy on<i>other</i>.    */
specifier|public
name|THRegionLocation
parameter_list|(
name|THRegionLocation
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|.
name|isSetServerName
argument_list|()
condition|)
block|{
name|this
operator|.
name|serverName
operator|=
operator|new
name|TServerName
argument_list|(
name|other
operator|.
name|serverName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|.
name|isSetRegionInfo
argument_list|()
condition|)
block|{
name|this
operator|.
name|regionInfo
operator|=
operator|new
name|THRegionInfo
argument_list|(
name|other
operator|.
name|regionInfo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|THRegionLocation
name|deepCopy
parameter_list|()
block|{
return|return
operator|new
name|THRegionLocation
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|this
operator|.
name|serverName
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
specifier|public
name|TServerName
name|getServerName
parameter_list|()
block|{
return|return
name|this
operator|.
name|serverName
return|;
block|}
specifier|public
name|THRegionLocation
name|setServerName
parameter_list|(
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
name|TServerName
name|serverName
parameter_list|)
block|{
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|void
name|unsetServerName
parameter_list|()
block|{
name|this
operator|.
name|serverName
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Returns true if field serverName is set (has been assigned a value) and false otherwise */
specifier|public
name|boolean
name|isSetServerName
parameter_list|()
block|{
return|return
name|this
operator|.
name|serverName
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|setServerNameIsSet
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
name|value
condition|)
block|{
name|this
operator|.
name|serverName
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
specifier|public
name|THRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionInfo
return|;
block|}
specifier|public
name|THRegionLocation
name|setRegionInfo
parameter_list|(
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
name|THRegionInfo
name|regionInfo
parameter_list|)
block|{
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|void
name|unsetRegionInfo
parameter_list|()
block|{
name|this
operator|.
name|regionInfo
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Returns true if field regionInfo is set (has been assigned a value) and false otherwise */
specifier|public
name|boolean
name|isSetRegionInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionInfo
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|setRegionInfoIsSet
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
name|value
condition|)
block|{
name|this
operator|.
name|regionInfo
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setFieldValue
parameter_list|(
name|_Fields
name|field
parameter_list|,
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
name|java
operator|.
name|lang
operator|.
name|Object
name|value
parameter_list|)
block|{
switch|switch
condition|(
name|field
condition|)
block|{
case|case
name|SERVER_NAME
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetServerName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setServerName
argument_list|(
operator|(
name|TServerName
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|REGION_INFO
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetRegionInfo
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setRegionInfo
argument_list|(
operator|(
name|THRegionInfo
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
specifier|public
name|java
operator|.
name|lang
operator|.
name|Object
name|getFieldValue
parameter_list|(
name|_Fields
name|field
parameter_list|)
block|{
switch|switch
condition|(
name|field
condition|)
block|{
case|case
name|SERVER_NAME
case|:
return|return
name|getServerName
argument_list|()
return|;
case|case
name|REGION_INFO
case|:
return|return
name|getRegionInfo
argument_list|()
return|;
block|}
throw|throw
operator|new
name|java
operator|.
name|lang
operator|.
name|IllegalStateException
argument_list|()
throw|;
block|}
comment|/** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
specifier|public
name|boolean
name|isSet
parameter_list|(
name|_Fields
name|field
parameter_list|)
block|{
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|java
operator|.
name|lang
operator|.
name|IllegalArgumentException
argument_list|()
throw|;
block|}
switch|switch
condition|(
name|field
condition|)
block|{
case|case
name|SERVER_NAME
case|:
return|return
name|isSetServerName
argument_list|()
return|;
case|case
name|REGION_INFO
case|:
return|return
name|isSetRegionInfo
argument_list|()
return|;
block|}
throw|throw
operator|new
name|java
operator|.
name|lang
operator|.
name|IllegalStateException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|java
operator|.
name|lang
operator|.
name|Object
name|that
parameter_list|)
block|{
if|if
condition|(
name|that
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|that
operator|instanceof
name|THRegionLocation
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|THRegionLocation
operator|)
name|that
argument_list|)
return|;
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|THRegionLocation
name|that
parameter_list|)
block|{
if|if
condition|(
name|that
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|this
operator|==
name|that
condition|)
return|return
literal|true
return|;
name|boolean
name|this_present_serverName
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetServerName
argument_list|()
decl_stmt|;
name|boolean
name|that_present_serverName
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetServerName
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_serverName
operator|||
name|that_present_serverName
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_serverName
operator|&&
name|that_present_serverName
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|this
operator|.
name|serverName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|serverName
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_regionInfo
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetRegionInfo
argument_list|()
decl_stmt|;
name|boolean
name|that_present_regionInfo
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetRegionInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_regionInfo
operator|||
name|that_present_regionInfo
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_regionInfo
operator|&&
name|that_present_regionInfo
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|this
operator|.
name|regionInfo
operator|.
name|equals
argument_list|(
name|that
operator|.
name|regionInfo
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hashCode
init|=
literal|1
decl_stmt|;
name|hashCode
operator|=
name|hashCode
operator|*
literal|8191
operator|+
operator|(
operator|(
name|isSetServerName
argument_list|()
operator|)
condition|?
literal|131071
else|:
literal|524287
operator|)
expr_stmt|;
if|if
condition|(
name|isSetServerName
argument_list|()
condition|)
name|hashCode
operator|=
name|hashCode
operator|*
literal|8191
operator|+
name|serverName
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|hashCode
operator|=
name|hashCode
operator|*
literal|8191
operator|+
operator|(
operator|(
name|isSetRegionInfo
argument_list|()
operator|)
condition|?
literal|131071
else|:
literal|524287
operator|)
expr_stmt|;
if|if
condition|(
name|isSetRegionInfo
argument_list|()
condition|)
name|hashCode
operator|=
name|hashCode
operator|*
literal|8191
operator|+
name|regionInfo
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|hashCode
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|THRegionLocation
name|other
parameter_list|)
block|{
if|if
condition|(
operator|!
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
name|int
name|lastComparison
init|=
literal|0
decl_stmt|;
name|lastComparison
operator|=
name|java
operator|.
name|lang
operator|.
name|Boolean
operator|.
name|valueOf
argument_list|(
name|isSetServerName
argument_list|()
argument_list|)
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|isSetServerName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastComparison
operator|!=
literal|0
condition|)
block|{
return|return
name|lastComparison
return|;
block|}
if|if
condition|(
name|isSetServerName
argument_list|()
condition|)
block|{
name|lastComparison
operator|=
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TBaseHelper
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|serverName
argument_list|,
name|other
operator|.
name|serverName
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastComparison
operator|!=
literal|0
condition|)
block|{
return|return
name|lastComparison
return|;
block|}
block|}
name|lastComparison
operator|=
name|java
operator|.
name|lang
operator|.
name|Boolean
operator|.
name|valueOf
argument_list|(
name|isSetRegionInfo
argument_list|()
argument_list|)
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|isSetRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastComparison
operator|!=
literal|0
condition|)
block|{
return|return
name|lastComparison
return|;
block|}
if|if
condition|(
name|isSetRegionInfo
argument_list|()
condition|)
block|{
name|lastComparison
operator|=
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TBaseHelper
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|regionInfo
argument_list|,
name|other
operator|.
name|regionInfo
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastComparison
operator|!=
literal|0
condition|)
block|{
return|return
name|lastComparison
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|annotation
operator|.
name|Nullable
specifier|public
name|_Fields
name|fieldForId
parameter_list|(
name|int
name|fieldId
parameter_list|)
block|{
return|return
name|_Fields
operator|.
name|findByThriftId
argument_list|(
name|fieldId
argument_list|)
return|;
block|}
specifier|public
name|void
name|read
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|iprot
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
name|scheme
argument_list|(
name|iprot
argument_list|)
operator|.
name|read
argument_list|(
name|iprot
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|oprot
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
name|scheme
argument_list|(
name|oprot
argument_list|)
operator|.
name|write
argument_list|(
name|oprot
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|java
operator|.
name|lang
operator|.
name|String
name|toString
parameter_list|()
block|{
name|java
operator|.
name|lang
operator|.
name|StringBuilder
name|sb
init|=
operator|new
name|java
operator|.
name|lang
operator|.
name|StringBuilder
argument_list|(
literal|"THRegionLocation("
argument_list|)
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"serverName:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|serverName
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|serverName
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|!
name|first
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"regionInfo:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|regionInfo
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|regionInfo
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|validate
parameter_list|()
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
comment|// check for required fields
if|if
condition|(
name|serverName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolException
argument_list|(
literal|"Required field 'serverName' was not present! Struct: "
operator|+
name|toString
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|regionInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolException
argument_list|(
literal|"Required field 'regionInfo' was not present! Struct: "
operator|+
name|toString
argument_list|()
argument_list|)
throw|;
block|}
comment|// check for sub-struct validity
if|if
condition|(
name|serverName
operator|!=
literal|null
condition|)
block|{
name|serverName
operator|.
name|validate
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|regionInfo
operator|!=
literal|null
condition|)
block|{
name|regionInfo
operator|.
name|validate
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|writeObject
parameter_list|(
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
name|out
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
block|{
try|try
block|{
name|write
argument_list|(
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TCompactProtocol
argument_list|(
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TIOStreamTransport
argument_list|(
name|out
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
name|te
parameter_list|)
block|{
throw|throw
operator|new
name|java
operator|.
name|io
operator|.
name|IOException
argument_list|(
name|te
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|readObject
parameter_list|(
name|java
operator|.
name|io
operator|.
name|ObjectInputStream
name|in
parameter_list|)
throws|throws
name|java
operator|.
name|io
operator|.
name|IOException
throws|,
name|java
operator|.
name|lang
operator|.
name|ClassNotFoundException
block|{
try|try
block|{
name|read
argument_list|(
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TCompactProtocol
argument_list|(
operator|new
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TIOStreamTransport
argument_list|(
name|in
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
name|te
parameter_list|)
block|{
throw|throw
operator|new
name|java
operator|.
name|io
operator|.
name|IOException
argument_list|(
name|te
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
class|class
name|THRegionLocationStandardSchemeFactory
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|SchemeFactory
block|{
specifier|public
name|THRegionLocationStandardScheme
name|getScheme
parameter_list|()
block|{
return|return
operator|new
name|THRegionLocationStandardScheme
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|THRegionLocationStandardScheme
extends|extends
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|StandardScheme
argument_list|<
name|THRegionLocation
argument_list|>
block|{
specifier|public
name|void
name|read
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|iprot
parameter_list|,
name|THRegionLocation
name|struct
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TField
name|schemeField
decl_stmt|;
name|iprot
operator|.
name|readStructBegin
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|schemeField
operator|=
name|iprot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
if|if
condition|(
name|schemeField
operator|.
name|type
operator|==
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TType
operator|.
name|STOP
condition|)
block|{
break|break;
block|}
switch|switch
condition|(
name|schemeField
operator|.
name|id
condition|)
block|{
case|case
literal|1
case|:
comment|// SERVER_NAME
if|if
condition|(
name|schemeField
operator|.
name|type
operator|==
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TType
operator|.
name|STRUCT
condition|)
block|{
name|struct
operator|.
name|serverName
operator|=
operator|new
name|TServerName
argument_list|()
expr_stmt|;
name|struct
operator|.
name|serverName
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
name|struct
operator|.
name|setServerNameIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|schemeField
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|2
case|:
comment|// REGION_INFO
if|if
condition|(
name|schemeField
operator|.
name|type
operator|==
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TType
operator|.
name|STRUCT
condition|)
block|{
name|struct
operator|.
name|regionInfo
operator|=
operator|new
name|THRegionInfo
argument_list|()
expr_stmt|;
name|struct
operator|.
name|regionInfo
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
name|struct
operator|.
name|setRegionInfoIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|schemeField
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolUtil
operator|.
name|skip
argument_list|(
name|iprot
argument_list|,
name|schemeField
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
name|iprot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
block|}
name|iprot
operator|.
name|readStructEnd
argument_list|()
expr_stmt|;
comment|// check for required fields of primitive type, which can't be checked in the validate method
name|struct
operator|.
name|validate
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|oprot
parameter_list|,
name|THRegionLocation
name|struct
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
name|struct
operator|.
name|validate
argument_list|()
expr_stmt|;
name|oprot
operator|.
name|writeStructBegin
argument_list|(
name|STRUCT_DESC
argument_list|)
expr_stmt|;
if|if
condition|(
name|struct
operator|.
name|serverName
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|SERVER_NAME_FIELD_DESC
argument_list|)
expr_stmt|;
name|struct
operator|.
name|serverName
operator|.
name|write
argument_list|(
name|oprot
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|struct
operator|.
name|regionInfo
operator|!=
literal|null
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|REGION_INFO_FIELD_DESC
argument_list|)
expr_stmt|;
name|struct
operator|.
name|regionInfo
operator|.
name|write
argument_list|(
name|oprot
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
name|oprot
operator|.
name|writeFieldStop
argument_list|()
expr_stmt|;
name|oprot
operator|.
name|writeStructEnd
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|THRegionLocationTupleSchemeFactory
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|SchemeFactory
block|{
specifier|public
name|THRegionLocationTupleScheme
name|getScheme
parameter_list|()
block|{
return|return
operator|new
name|THRegionLocationTupleScheme
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|THRegionLocationTupleScheme
extends|extends
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|TupleScheme
argument_list|<
name|THRegionLocation
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|prot
parameter_list|,
name|THRegionLocation
name|struct
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TTupleProtocol
name|oprot
init|=
operator|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TTupleProtocol
operator|)
name|prot
decl_stmt|;
name|struct
operator|.
name|serverName
operator|.
name|write
argument_list|(
name|oprot
argument_list|)
expr_stmt|;
name|struct
operator|.
name|regionInfo
operator|.
name|write
argument_list|(
name|oprot
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|read
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|prot
parameter_list|,
name|THRegionLocation
name|struct
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
block|{
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TTupleProtocol
name|iprot
init|=
operator|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TTupleProtocol
operator|)
name|prot
decl_stmt|;
name|struct
operator|.
name|serverName
operator|=
operator|new
name|TServerName
argument_list|()
expr_stmt|;
name|struct
operator|.
name|serverName
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
name|struct
operator|.
name|setServerNameIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|struct
operator|.
name|regionInfo
operator|=
operator|new
name|THRegionInfo
argument_list|()
expr_stmt|;
name|struct
operator|.
name|regionInfo
operator|.
name|read
argument_list|(
name|iprot
argument_list|)
expr_stmt|;
name|struct
operator|.
name|setRegionInfoIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
parameter_list|<
name|S
extends|extends
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|IScheme
parameter_list|>
name|S
name|scheme
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
name|proto
parameter_list|)
block|{
return|return
operator|(
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|StandardScheme
operator|.
name|class
operator|.
name|equals
argument_list|(
name|proto
operator|.
name|getScheme
argument_list|()
argument_list|)
condition|?
name|STANDARD_SCHEME_FACTORY
else|:
name|TUPLE_SCHEME_FACTORY
operator|)
operator|.
name|getScheme
argument_list|()
return|;
block|}
block|}
end_class

end_unit

