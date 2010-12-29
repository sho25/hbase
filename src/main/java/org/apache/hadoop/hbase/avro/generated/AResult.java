begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Avro  *   * DO NOT EDIT DIRECTLY  */
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
name|avro
operator|.
name|generated
package|;
end_package

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"all"
argument_list|)
specifier|public
class|class
name|AResult
extends|extends
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|specific
operator|.
name|SpecificRecordBase
implements|implements
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|specific
operator|.
name|SpecificRecord
block|{
specifier|public
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
name|SCHEMA$
init|=
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
operator|.
name|parse
argument_list|(
literal|"{\"type\":\"record\",\"name\":\"AResult\",\"namespace\":\"org.apache.hadoop.hbase.avro.generated\",\"fields\":[{\"name\":\"row\",\"type\":\"bytes\"},{\"name\":\"entries\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"AResultEntry\",\"fields\":[{\"name\":\"family\",\"type\":\"bytes\"},{\"name\":\"qualifier\",\"type\":\"bytes\"},{\"name\":\"value\",\"type\":\"bytes\"},{\"name\":\"timestamp\",\"type\":\"long\"}]}}}]}"
argument_list|)
decl_stmt|;
specifier|public
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|row
decl_stmt|;
specifier|public
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AResultEntry
argument_list|>
name|entries
decl_stmt|;
specifier|public
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
name|getSchema
parameter_list|()
block|{
return|return
name|SCHEMA$
return|;
block|}
comment|// Used by DatumWriter.  Applications should not call.
specifier|public
name|java
operator|.
name|lang
operator|.
name|Object
name|get
parameter_list|(
name|int
name|field$
parameter_list|)
block|{
switch|switch
condition|(
name|field$
condition|)
block|{
case|case
literal|0
case|:
return|return
name|row
return|;
case|case
literal|1
case|:
return|return
name|entries
return|;
default|default:
throw|throw
operator|new
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|AvroRuntimeException
argument_list|(
literal|"Bad index"
argument_list|)
throw|;
block|}
block|}
comment|// Used by DatumReader.  Applications should not call.
annotation|@
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|put
parameter_list|(
name|int
name|field$
parameter_list|,
name|java
operator|.
name|lang
operator|.
name|Object
name|value$
parameter_list|)
block|{
switch|switch
condition|(
name|field$
condition|)
block|{
case|case
literal|0
case|:
name|row
operator|=
operator|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
operator|)
name|value$
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|entries
operator|=
operator|(
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AResultEntry
argument_list|>
operator|)
name|value$
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|AvroRuntimeException
argument_list|(
literal|"Bad index"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

