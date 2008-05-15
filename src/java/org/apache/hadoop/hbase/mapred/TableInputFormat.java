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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|Path
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
name|HBaseConfiguration
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
name|util
operator|.
name|Bytes
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
name|JobConfigurable
import|;
end_import

begin_comment
comment|/**  * Convert HBase tabular data into a format that is consumable by Map/Reduce.  */
end_comment

begin_class
specifier|public
class|class
name|TableInputFormat
extends|extends
name|TableInputFormatBase
implements|implements
name|JobConfigurable
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TableInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * space delimited list of columns    *    * @see org.apache.hadoop.hbase.regionserver.HAbstractScanner for column name    *      wildcards    */
specifier|public
specifier|static
specifier|final
name|String
name|COLUMN_LIST
init|=
literal|"hbase.mapred.tablecolumns"
decl_stmt|;
comment|/** {@inheritDoc} */
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|Path
index|[]
name|tableNames
init|=
name|job
operator|.
name|getInputPaths
argument_list|()
decl_stmt|;
name|String
name|colArg
init|=
name|job
operator|.
name|get
argument_list|(
name|COLUMN_LIST
argument_list|)
decl_stmt|;
name|String
index|[]
name|colNames
init|=
name|colArg
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|m_cols
init|=
operator|new
name|byte
index|[
name|colNames
operator|.
name|length
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|m_cols
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|m_cols
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|colNames
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|setInputColums
argument_list|(
name|m_cols
argument_list|)
expr_stmt|;
try|try
block|{
name|setHTable
argument_list|(
operator|new
name|HTable
argument_list|(
operator|new
name|HBaseConfiguration
argument_list|(
name|job
argument_list|)
argument_list|,
name|tableNames
index|[
literal|0
index|]
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
block|}
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|validateInput
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
comment|// expecting exactly one path
name|Path
index|[]
name|tableNames
init|=
name|job
operator|.
name|getInputPaths
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableNames
operator|==
literal|null
operator|||
name|tableNames
operator|.
name|length
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"expecting one table name"
argument_list|)
throw|;
block|}
comment|// expecting at least one column
name|String
name|colArg
init|=
name|job
operator|.
name|get
argument_list|(
name|COLUMN_LIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|colArg
operator|==
literal|null
operator|||
name|colArg
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"expecting at least one column"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

