begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapred
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
name|Map
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
name|fs
operator|.
name|FileSystem
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
name|io
operator|.
name|MapWritable
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
name|io
operator|.
name|Text
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
name|io
operator|.
name|Writable
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
name|mapred
operator|.
name|FileAlreadyExistsException
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
name|mapred
operator|.
name|InvalidJobConfException
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|OutputFormatBase
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
name|mapred
operator|.
name|RecordWriter
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
name|mapred
operator|.
name|Reporter
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
name|util
operator|.
name|Progressable
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
name|HTable
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
name|io
operator|.
name|ImmutableBytesWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  * Convert Map/Reduce output and write it to an HBase table  */
end_comment

begin_class
specifier|public
class|class
name|TableOutputFormat
extends|extends
name|OutputFormatBase
argument_list|<
name|Text
argument_list|,
name|MapWritable
argument_list|>
block|{
comment|/** JobConf parameter that specifies the output table */
specifier|public
specifier|static
specifier|final
name|String
name|OUTPUT_TABLE
init|=
literal|"hbase.mapred.outputtable"
decl_stmt|;
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|TableOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/** constructor */
specifier|public
name|TableOutputFormat
parameter_list|()
block|{}
comment|/**    * Convert Reduce output (key, value) to (HStoreKey, KeyedDataArrayWritable)     * and write to an HBase table    */
specifier|protected
class|class
name|TableRecordWriter
implements|implements
name|RecordWriter
argument_list|<
name|Text
argument_list|,
name|MapWritable
argument_list|>
block|{
specifier|private
name|HTable
name|m_table
decl_stmt|;
comment|/**      * Instantiate a TableRecordWriter with the HBase HClient for writing.      *       * @param table      */
specifier|public
name|TableRecordWriter
parameter_list|(
name|HTable
name|table
parameter_list|)
block|{
name|m_table
operator|=
name|table
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|close
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Reporter
name|reporter
parameter_list|)
block|{}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|write
parameter_list|(
name|Text
name|key
parameter_list|,
name|MapWritable
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|xid
init|=
name|m_table
operator|.
name|startUpdate
argument_list|(
name|key
argument_list|)
decl_stmt|;
comment|// start transaction
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Writable
argument_list|,
name|Writable
argument_list|>
name|e
range|:
name|value
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|m_table
operator|.
name|put
argument_list|(
name|xid
argument_list|,
operator|(
name|Text
operator|)
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
operator|(
name|ImmutableBytesWritable
operator|)
name|e
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|m_table
operator|.
name|commit
argument_list|(
name|xid
argument_list|)
expr_stmt|;
comment|// end transaction
block|}
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|RecordWriter
name|getRecordWriter
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|String
name|name
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
comment|// expecting exactly one path
name|Text
name|tableName
init|=
operator|new
name|Text
argument_list|(
name|job
operator|.
name|get
argument_list|(
name|OUTPUT_TABLE
argument_list|)
argument_list|)
decl_stmt|;
name|HTable
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|job
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
operator|new
name|TableRecordWriter
argument_list|(
name|table
argument_list|)
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|FileAlreadyExistsException
throws|,
name|InvalidJobConfException
throws|,
name|IOException
block|{
name|String
name|tableName
init|=
name|job
operator|.
name|get
argument_list|(
name|OUTPUT_TABLE
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Must specify table name"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

