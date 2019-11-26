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
literal|"2019-11-07"
argument_list|)
specifier|public
class|class
name|TCellVisibility
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TBase
argument_list|<
name|TCellVisibility
argument_list|,
name|TCellVisibility
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
name|TCellVisibility
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
literal|"TCellVisibility"
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
name|EXPRESSION_FIELD_DESC
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
literal|"expression"
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
name|STRING
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
name|scheme
operator|.
name|SchemeFactory
name|STANDARD_SCHEME_FACTORY
init|=
operator|new
name|TCellVisibilityStandardSchemeFactory
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
name|TCellVisibilityTupleSchemeFactory
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
name|java
operator|.
name|lang
operator|.
name|String
name|expression
decl_stmt|;
comment|// optional
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
name|EXPRESSION
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|,
literal|"expression"
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
comment|// EXPRESSION
return|return
name|EXPRESSION
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
specifier|private
specifier|static
specifier|final
name|_Fields
name|optionals
index|[]
init|=
block|{
name|_Fields
operator|.
name|EXPRESSION
block|}
decl_stmt|;
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
name|EXPRESSION
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
literal|"expression"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TFieldRequirementType
operator|.
name|OPTIONAL
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
name|STRING
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
name|TCellVisibility
operator|.
name|class
argument_list|,
name|metaDataMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TCellVisibility
parameter_list|()
block|{   }
comment|/**    * Performs a deep copy on<i>other</i>.    */
specifier|public
name|TCellVisibility
parameter_list|(
name|TCellVisibility
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|.
name|isSetExpression
argument_list|()
condition|)
block|{
name|this
operator|.
name|expression
operator|=
name|other
operator|.
name|expression
expr_stmt|;
block|}
block|}
specifier|public
name|TCellVisibility
name|deepCopy
parameter_list|()
block|{
return|return
operator|new
name|TCellVisibility
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
name|expression
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
name|java
operator|.
name|lang
operator|.
name|String
name|getExpression
parameter_list|()
block|{
return|return
name|this
operator|.
name|expression
return|;
block|}
specifier|public
name|TCellVisibility
name|setExpression
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
name|java
operator|.
name|lang
operator|.
name|String
name|expression
parameter_list|)
block|{
name|this
operator|.
name|expression
operator|=
name|expression
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|void
name|unsetExpression
parameter_list|()
block|{
name|this
operator|.
name|expression
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Returns true if field expression is set (has been assigned a value) and false otherwise */
specifier|public
name|boolean
name|isSetExpression
parameter_list|()
block|{
return|return
name|this
operator|.
name|expression
operator|!=
literal|null
return|;
block|}
specifier|public
name|void
name|setExpressionIsSet
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
name|expression
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
name|EXPRESSION
case|:
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|unsetExpression
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|setExpression
argument_list|(
operator|(
name|java
operator|.
name|lang
operator|.
name|String
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
name|EXPRESSION
case|:
return|return
name|getExpression
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
name|EXPRESSION
case|:
return|return
name|isSetExpression
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
name|TCellVisibility
condition|)
return|return
name|this
operator|.
name|equals
argument_list|(
operator|(
name|TCellVisibility
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
name|TCellVisibility
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
name|this_present_expression
init|=
literal|true
operator|&&
name|this
operator|.
name|isSetExpression
argument_list|()
decl_stmt|;
name|boolean
name|that_present_expression
init|=
literal|true
operator|&&
name|that
operator|.
name|isSetExpression
argument_list|()
decl_stmt|;
if|if
condition|(
name|this_present_expression
operator|||
name|that_present_expression
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|this_present_expression
operator|&&
name|that_present_expression
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
name|expression
operator|.
name|equals
argument_list|(
name|that
operator|.
name|expression
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
name|isSetExpression
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
name|isSetExpression
argument_list|()
condition|)
name|hashCode
operator|=
name|hashCode
operator|*
literal|8191
operator|+
name|expression
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
name|TCellVisibility
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
name|isSetExpression
argument_list|()
argument_list|)
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|isSetExpression
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
name|isSetExpression
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
name|expression
argument_list|,
name|other
operator|.
name|expression
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
literal|"TCellVisibility("
argument_list|)
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|isSetExpression
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"expression:"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|expression
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
name|expression
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
block|}
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
name|TCellVisibilityStandardSchemeFactory
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
name|TCellVisibilityStandardScheme
name|getScheme
parameter_list|()
block|{
return|return
operator|new
name|TCellVisibilityStandardScheme
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TCellVisibilityStandardScheme
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
name|TCellVisibility
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
name|TCellVisibility
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
comment|// EXPRESSION
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
name|STRING
condition|)
block|{
name|struct
operator|.
name|expression
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|struct
operator|.
name|setExpressionIsSet
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
name|TCellVisibility
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
name|expression
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|struct
operator|.
name|isSetExpression
argument_list|()
condition|)
block|{
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|EXPRESSION_FIELD_DESC
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeString
argument_list|(
name|struct
operator|.
name|expression
argument_list|)
expr_stmt|;
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
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
name|TCellVisibilityTupleSchemeFactory
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
name|TCellVisibilityTupleScheme
name|getScheme
parameter_list|()
block|{
return|return
operator|new
name|TCellVisibilityTupleScheme
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TCellVisibilityTupleScheme
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
name|TCellVisibility
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
name|TCellVisibility
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
name|java
operator|.
name|util
operator|.
name|BitSet
name|optionals
init|=
operator|new
name|java
operator|.
name|util
operator|.
name|BitSet
argument_list|()
decl_stmt|;
if|if
condition|(
name|struct
operator|.
name|isSetExpression
argument_list|()
condition|)
block|{
name|optionals
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|oprot
operator|.
name|writeBitSet
argument_list|(
name|optionals
argument_list|,
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|struct
operator|.
name|isSetExpression
argument_list|()
condition|)
block|{
name|oprot
operator|.
name|writeString
argument_list|(
name|struct
operator|.
name|expression
argument_list|)
expr_stmt|;
block|}
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
name|TCellVisibility
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
name|java
operator|.
name|util
operator|.
name|BitSet
name|incoming
init|=
name|iprot
operator|.
name|readBitSet
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|incoming
operator|.
name|get
argument_list|(
literal|0
argument_list|)
condition|)
block|{
name|struct
operator|.
name|expression
operator|=
name|iprot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|struct
operator|.
name|setExpressionIsSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
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

