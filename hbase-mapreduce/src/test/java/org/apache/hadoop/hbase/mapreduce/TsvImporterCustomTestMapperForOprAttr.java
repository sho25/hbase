begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Put
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|mapreduce
operator|.
name|ImportTsv
operator|.
name|TsvParser
operator|.
name|BadTsvLineException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|mapreduce
operator|.
name|ImportTsv
operator|.
name|TsvParser
operator|.
name|ParsedLine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Just shows a simple example of how the attributes can be extracted and added  * to the puts  */
end_comment

begin_class
specifier|public
class|class
name|TsvImporterCustomTestMapperForOprAttr
extends|extends
name|TsvImporterMapper
block|{
annotation|@
name|Override
specifier|protected
name|void
name|populatePut
parameter_list|(
name|byte
index|[]
name|lineBytes
parameter_list|,
name|ParsedLine
name|parsed
parameter_list|,
name|Put
name|put
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|BadTsvLineException
throws|,
name|IOException
block|{
name|KeyValue
name|kv
decl_stmt|;
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|lineBytes
argument_list|,
name|parsed
operator|.
name|getRowKeyOffset
argument_list|()
argument_list|,
name|parsed
operator|.
name|getRowKeyLength
argument_list|()
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
name|i
argument_list|)
argument_list|,
literal|0
argument_list|,
name|parser
operator|.
name|getFamily
argument_list|(
name|i
argument_list|)
operator|.
name|length
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
name|i
argument_list|)
argument_list|,
literal|0
argument_list|,
name|parser
operator|.
name|getQualifier
argument_list|(
name|i
argument_list|)
operator|.
name|length
argument_list|,
name|ts
argument_list|,
name|KeyValue
operator|.
name|Type
operator|.
name|Put
argument_list|,
name|lineBytes
argument_list|,
name|parsed
operator|.
name|getColumnOffset
argument_list|(
name|i
argument_list|)
argument_list|,
name|parsed
operator|.
name|getColumnLength
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|parsed
operator|.
name|getIndividualAttributes
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|attributes
init|=
name|parsed
operator|.
name|getIndividualAttributes
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|attr
range|:
name|attributes
control|)
block|{
name|String
index|[]
name|split
init|=
name|attr
operator|.
name|split
argument_list|(
name|ImportTsv
operator|.
name|DEFAULT_ATTRIBUTES_SEPERATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|split
operator|==
literal|null
operator|||
name|split
operator|.
name|length
operator|<=
literal|1
condition|)
block|{
throw|throw
operator|new
name|BadTsvLineException
argument_list|(
name|msg
argument_list|(
name|attributes
argument_list|)
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
name|split
index|[
literal|0
index|]
operator|.
name|length
argument_list|()
operator|<=
literal|0
operator|||
name|split
index|[
literal|1
index|]
operator|.
name|length
argument_list|()
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|BadTsvLineException
argument_list|(
name|msg
argument_list|(
name|attributes
argument_list|)
argument_list|)
throw|;
block|}
name|put
operator|.
name|setAttribute
argument_list|(
name|split
index|[
literal|0
index|]
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|split
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|put
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|msg
parameter_list|(
name|Object
index|[]
name|attributes
parameter_list|)
block|{
return|return
literal|"Invalid attributes separator specified: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|attributes
argument_list|)
return|;
block|}
block|}
end_class

end_unit

