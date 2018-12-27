begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift Compiler (0.9.3)  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  *  @generated  */
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|IScheme
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|SchemeFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|StandardScheme
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|TupleScheme
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TTupleProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|EncodingUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|async
operator|.
name|AsyncMethodCallback
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|AbstractNonblockingServer
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|BitSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Generated
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

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
block|}
argument_list|)
annotation|@
name|Generated
argument_list|(
name|value
operator|=
literal|"Autogenerated by Thrift Compiler (0.9.3)"
argument_list|,
name|date
operator|=
literal|"2018-12-27"
argument_list|)
specifier|public
class|class
name|TTimeRange
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TBase
argument_list|<
name|TTimeRange
argument_list|,
name|TTimeRange
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
name|TTimeRange
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
literal|"TTimeRange"
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
name|MIN_STAMP_FIELD_DESC
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
literal|"minStamp"
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
name|I64
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
name|MAX_STAMP_FIELD_DESC
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
literal|"maxStamp"
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
name|I64
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
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|IScheme
argument_list|>
argument_list|,
name|SchemeFactory
argument_list|>
name|schemes
init|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|IScheme
argument_list|>
argument_list|,
name|SchemeFactory
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|schemes
operator|.
name|put
argument_list|(
name|StandardScheme
operator|.
name|class
argument_list|,
operator|new
name|TTimeRangeStandardSchemeFactory
argument_list|()
argument_list|)
expr_stmt|;
name|schemes
operator|.
name|put
argument_list|(
name|TupleScheme
operator|.
name|class
argument_list|,
operator|new
name|TTimeRangeTupleSchemeFactory
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|minStamp
decl_stmt|;
comment|// required
specifier|public
name|long
name|maxStamp
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
name|MIN_STAMP
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|,
literal|"minStamp"
argument_list|)
block|,
name|MAX_STAMP
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|,
literal|"maxStamp"
argument_list|)
block|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|_Fields
argument_list|>
name|byName
init|=
operator|new
name|HashMap
argument_list|<
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
comment|// MIN_STAMP
return|return
name|MIN_STAMP
return|;
case|case
literal|2
case|:
comment|// MAX_STAMP
return|return
name|MAX_STAMP
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
specifier|public
specifier|static
name|_Fields
name|findByName
parameter_list|(
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
name|String
name|_fieldName
decl_stmt|;
name|_Fields
parameter_list|(
name|short
name|thriftId
parameter_list|,
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
specifier|private
specifier|static
specifier|final
name|int
name|__MINSTAMP_ISSET_ID
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|__MAXSTAMP_ISSET_ID
init|=
literal|1
decl_stmt|;
specifier|private
name|byte
name|__isset_bitfield
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
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
name|MIN_STAMP
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
literal|"minStamp"
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
name|FieldValueMetaData
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
name|I64
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
name|MAX_STAMP
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
literal|"maxStamp"
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
name|FieldValueMetaData
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
name|I64
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|metaDataMap
operator|=
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
name|TTimeRange
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TTimeRange
parameter_list|()
block|{   }
specifier|public
name|TTimeRange
parameter_list|(
name|long
name|minStamp
parameter_list|,
name|long
name|maxStamp
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|minStamp
operator|=
name|minStamp
expr_stmt|;
name|setMinStampIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxStamp
operator|=
name|maxStamp
expr_stmt|;
name|setMaxStampIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Performs a deep copy on<i>other</i>.    */
specifier|public
name|TTimeRange
parameter_list|(
name|TTimeRange
name|other
parameter_list|)
block|{
name|__isset_bitfield
operator|=
name|other
operator|.
name|__isset_bitfield
expr_stmt|;
name|this
operator|.
name|minStamp
operator|=
name|other
operator|.
name|minStamp
expr_stmt|;
name|this
operator|.
name|maxStamp
operator|=
name|other
operator|.
name|maxStamp
expr_stmt|;
block|}
specifier|public
name|TTimeRange
name|deepCopy
parameter_list|()
block|{
return|return
operator|new
name|TTimeRange
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
name|setMinStampIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|minStamp
operator|=
literal|0
expr_stmt|;
name|setMaxStampIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxStamp
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|long
name|getMinStamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|minStamp
return|;
block|}
specifier|public
name|TTimeRange
name|setMinStamp
parameter_list|(
name|long
name|minStamp
parameter_list|)
block|{
name|this
operator|.
name|minStamp
operator|=
name|minStamp
expr_stmt|;
name|setMinStampIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|void
name|unsetMinStamp
parameter_list|()
block|{
name|__isset_bitfield
operator|=
name|EncodingUtils
operator|.
name|clearBit
argument_list|(
name|__isset_bitfield
argument_list|,
name|__MINSTAMP_ISSET_ID
argument_list|)
expr_stmt|;
block|}
comment|/** Returns true if field minStamp is set (has been assigned a value) and false otherwise */
specifier|public
name|boolean
name|isSetMinStamp
parameter_list|()
block|{
return|return
name|EncodingUtils
operator|.
name|testBit
argument_list|(
name|__isset_bitfield
argument_list|,
name|__MINSTAMP_ISSET_ID
argument_list|)
return|;
block|}
specifier|public
name|void
name|setMinStampIsSet
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|__isset_bitfield
operator|=
name|EncodingUtils
operator|.
name|setBit
argument_list|(
name|__isset_bitfield
argument_list|,
name|__MINSTAMP_ISSET_ID
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getMaxStamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxStamp
return|;
block|}
specifier|public
name|TTimeRange
name|setMaxStamp
parameter_list|(
name|long
name|maxStamp
parameter_list|)
block|{
name|this
operator|.
name|maxStamp
operator|=
name|maxStamp
expr_stmt|;
name|setMaxStampIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|void
name|unsetMaxStamp
parameter_list|()
block|{
name|__isset_bitfield
operator|=
name|EncodingUtils
operator|.
name|clearBit
argument_list|(
name|__isset_bitfield
argument_list|,
name|__MAXSTAMP_ISSET_ID
argument_list|)
expr_stmt|;
block|}
comment|/** Returns true if field maxStamp is set (has been assigned a value) and false otherwise */
specifier|public
name|boolean
name|isSetMaxStamp
parameter_list|()
block|{
return|return
name|EncodingUtils
operator|.
name|testBit
argument_list|(
name|__isset_bitfield
argument_list|,
name|__MAXSTAMP_ISSET_ID
argument_list|)
return|;
block|}
specifier|public
name|void
name|setMaxStampIsSet
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|__isset_bitfield
operator|=
name|EncodingUtils
operator|.
name|setBit
argument_list|(
name|__isset_bitfield
argument_list|,
name|__MAXSTAMP_ISSET_ID
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setFieldValue
parameter_list|(
name|_Fields
name|field
parameter_list|,
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
name|MIN_STAMP
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetMinStamp
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setMinStamp
argument_list|(
operator|(
name|Long
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|MAX_STAMP
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetMaxStamp
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setMaxStamp
argument_list|(
operator|(
name|Long
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
specifier|public
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
name|MIN_STAMP
case|:
return|return
name|getMinStamp
argument_list|()
return|;
case|case
name|MAX_STAMP
case|:
return|return
name|getMaxStamp
argument_list|()
return|;
block|}
throw|throw
operator|new
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
name|MIN_STAMP
case|:
return|return
name|isSetMinStamp
argument_list|()
return|;
case|case
name|MAX_STAMP
case|:
return|return
name|isSetMaxStamp
argument_list|()
return|;
block|}
throw|throw
operator|new
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
name|TTimeRange
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|TTimeRange
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
name|TTimeRange
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
name|boolean
name|this_present_minStamp
init|=
literal|true
decl_stmt|;
name|boolean
name|that_present_minStamp
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|this_present_minStamp
operator|||
name|that_present_minStamp
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_minStamp
operator|&&
name|that_present_minStamp
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|this
operator|.
name|minStamp
operator|!=
name|that
operator|.
name|minStamp
condition|)
return|return
literal|false
return|;
block|}
name|boolean
name|this_present_maxStamp
init|=
literal|true
decl_stmt|;
name|boolean
name|that_present_maxStamp
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|this_present_maxStamp
operator|||
name|that_present_maxStamp
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_maxStamp
operator|&&
name|that_present_maxStamp
operator|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|this
operator|.
name|maxStamp
operator|!=
name|that
operator|.
name|maxStamp
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
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|present_minStamp
init|=
literal|true
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|present_minStamp
argument_list|)
expr_stmt|;
if|if
condition|(
name|present_minStamp
condition|)
name|list
operator|.
name|add
argument_list|(
name|minStamp
argument_list|)
expr_stmt|;
name|boolean
name|present_maxStamp
init|=
literal|true
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|present_maxStamp
argument_list|)
expr_stmt|;
if|if
condition|(
name|present_maxStamp
condition|)
name|list
operator|.
name|add
argument_list|(
name|maxStamp
argument_list|)
expr_stmt|;
return|return
name|list
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|TTimeRange
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
name|Boolean
operator|.
name|valueOf
argument_list|(
name|isSetMinStamp
argument_list|()
argument_list|)
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|isSetMinStamp
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
name|isSetMinStamp
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
name|minStamp
argument_list|,
name|other
operator|.
name|minStamp
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
name|Boolean
operator|.
name|valueOf
argument_list|(
name|isSetMaxStamp
argument_list|()
argument_list|)
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|isSetMaxStamp
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
name|isSetMaxStamp
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
name|maxStamp
argument_list|,
name|other
operator|.
name|maxStamp
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
name|schemes
operator|.
name|get
argument_list|(
name|iprot
operator|.
name|getScheme
argument_list|()
argument_list|)
operator|.
name|getScheme
argument_list|()
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
name|schemes
operator|.
name|get
argument_list|(
name|oprot
operator|.
name|getScheme
argument_list|()
argument_list|)
operator|.
name|getScheme
argument_list|()
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
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"TTimeRange("
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
literal|"minStamp:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|minStamp
argument_list|)
expr_stmt|;
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
literal|"maxStamp:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|maxStamp
argument_list|)
expr_stmt|;
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
comment|// alas, we cannot check 'minStamp' because it's a primitive and you chose the non-beans generator.
comment|// alas, we cannot check 'maxStamp' because it's a primitive and you chose the non-beans generator.
comment|// check for sub-struct validity
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
name|ClassNotFoundException
block|{
try|try
block|{
comment|// it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
name|__isset_bitfield
operator|=
literal|0
expr_stmt|;
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
name|TTimeRangeStandardSchemeFactory
implements|implements
name|SchemeFactory
block|{
specifier|public
name|TTimeRangeStandardScheme
name|getScheme
parameter_list|()
block|{
return|return
operator|new
name|TTimeRangeStandardScheme
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TTimeRangeStandardScheme
extends|extends
name|StandardScheme
argument_list|<
name|TTimeRange
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
name|TTimeRange
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
comment|// MIN_STAMP
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
name|I64
condition|)
block|{
name|struct
operator|.
name|minStamp
operator|=
name|iprot
operator|.
name|readI64
argument_list|()
expr_stmt|;
name|struct
operator|.
name|setMinStampIsSet
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
comment|// MAX_STAMP
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
name|I64
condition|)
block|{
name|struct
operator|.
name|maxStamp
operator|=
name|iprot
operator|.
name|readI64
argument_list|()
expr_stmt|;
name|struct
operator|.
name|setMaxStampIsSet
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
if|if
condition|(
operator|!
name|struct
operator|.
name|isSetMinStamp
argument_list|()
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
literal|"Required field 'minStamp' was not found in serialized data! Struct: "
operator|+
name|toString
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|struct
operator|.
name|isSetMaxStamp
argument_list|()
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
literal|"Required field 'maxStamp' was not found in serialized data! Struct: "
operator|+
name|toString
argument_list|()
argument_list|)
throw|;
block|}
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
name|TTimeRange
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
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|MIN_STAMP_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeI64
argument_list|(
name|struct
operator|.
name|minStamp
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|MAX_STAMP_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeI64
argument_list|(
name|struct
operator|.
name|maxStamp
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
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
name|TTimeRangeTupleSchemeFactory
implements|implements
name|SchemeFactory
block|{
specifier|public
name|TTimeRangeTupleScheme
name|getScheme
parameter_list|()
block|{
return|return
operator|new
name|TTimeRangeTupleScheme
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TTimeRangeTupleScheme
extends|extends
name|TupleScheme
argument_list|<
name|TTimeRange
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
name|TTimeRange
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
name|TTupleProtocol
name|oprot
init|=
operator|(
name|TTupleProtocol
operator|)
name|prot
decl_stmt|;
name|oprot
operator|.
name|writeI64
argument_list|(
name|struct
operator|.
name|minStamp
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeI64
argument_list|(
name|struct
operator|.
name|maxStamp
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
name|TTimeRange
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
name|TTupleProtocol
name|iprot
init|=
operator|(
name|TTupleProtocol
operator|)
name|prot
decl_stmt|;
name|struct
operator|.
name|minStamp
operator|=
name|iprot
operator|.
name|readI64
argument_list|()
expr_stmt|;
name|struct
operator|.
name|setMinStampIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|struct
operator|.
name|maxStamp
operator|=
name|iprot
operator|.
name|readI64
argument_list|()
expr_stmt|;
name|struct
operator|.
name|setMaxStampIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

