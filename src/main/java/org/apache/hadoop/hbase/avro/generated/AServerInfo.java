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
name|AServerInfo
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
literal|"{\"type\":\"record\",\"name\":\"AServerInfo\",\"namespace\":\"org.apache.hadoop.hbase.avro.generated\",\"fields\":[{\"name\":\"infoPort\",\"type\":\"int\"},{\"name\":\"load\",\"type\":{\"type\":\"record\",\"name\":\"AServerLoad\",\"fields\":[{\"name\":\"load\",\"type\":\"int\"},{\"name\":\"maxHeapMB\",\"type\":\"int\"},{\"name\":\"memStoreSizeInMB\",\"type\":\"int\"},{\"name\":\"numberOfRegions\",\"type\":\"int\"},{\"name\":\"numberOfRequests\",\"type\":\"int\"},{\"name\":\"regionsLoad\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"ARegionLoad\",\"fields\":[{\"name\":\"memStoreSizeMB\",\"type\":\"int\"},{\"name\":\"name\",\"type\":\"bytes\"},{\"name\":\"storefileIndexSizeMB\",\"type\":\"int\"},{\"name\":\"storefiles\",\"type\":\"int\"},{\"name\":\"storefileSizeMB\",\"type\":\"int\"},{\"name\":\"stores\",\"type\":\"int\"}]}}},{\"name\":\"storefileIndexSizeInMB\",\"type\":\"int\"},{\"name\":\"storefiles\",\"type\":\"int\"},{\"name\":\"storefileSizeInMB\",\"type\":\"int\"},{\"name\":\"usedHeapMB\",\"type\":\"int\"}]}},{\"name\":\"serverAddress\",\"type\":{\"type\":\"record\",\"name\":\"AServerAddress\",\"fields\":[{\"name\":\"hostname\",\"type\":\"string\"},{\"name\":\"inetSocketAddress\",\"type\":\"string\"},{\"name\":\"port\",\"type\":\"int\"}]}},{\"name\":\"serverName\",\"type\":\"string\"},{\"name\":\"startCode\",\"type\":\"long\"}]}"
argument_list|)
decl_stmt|;
specifier|public
name|int
name|infoPort
decl_stmt|;
specifier|public
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
name|AServerLoad
name|load
decl_stmt|;
specifier|public
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
name|AServerAddress
name|serverAddress
decl_stmt|;
specifier|public
name|java
operator|.
name|lang
operator|.
name|CharSequence
name|serverName
decl_stmt|;
specifier|public
name|long
name|startCode
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
name|infoPort
return|;
case|case
literal|1
case|:
return|return
name|load
return|;
case|case
literal|2
case|:
return|return
name|serverAddress
return|;
case|case
literal|3
case|:
return|return
name|serverName
return|;
case|case
literal|4
case|:
return|return
name|startCode
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
name|infoPort
operator|=
operator|(
name|java
operator|.
name|lang
operator|.
name|Integer
operator|)
name|value$
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|load
operator|=
operator|(
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
name|AServerLoad
operator|)
name|value$
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|serverAddress
operator|=
operator|(
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
name|AServerAddress
operator|)
name|value$
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|serverName
operator|=
operator|(
name|java
operator|.
name|lang
operator|.
name|CharSequence
operator|)
name|value$
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|startCode
operator|=
operator|(
name|java
operator|.
name|lang
operator|.
name|Long
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

