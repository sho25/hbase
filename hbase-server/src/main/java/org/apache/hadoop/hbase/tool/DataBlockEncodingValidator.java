begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|tool
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
name|List
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
name|HBaseInterfaceAudience
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
name|Admin
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
name|ColumnFamilyDescriptor
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
name|Connection
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
name|ConnectionFactory
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
name|TableDescriptor
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
name|encoding
operator|.
name|DataBlockEncoding
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
name|AbstractHBaseTool
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
specifier|public
class|class
name|DataBlockEncodingValidator
extends|extends
name|AbstractHBaseTool
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|DataBlockEncodingValidator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|DATA_BLOCK_ENCODING
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"DATA_BLOCK_ENCODING"
argument_list|)
decl_stmt|;
comment|/**    * Check DataBlockEncodings of column families are compatible.    *    * @return number of column families with incompatible DataBlockEncoding    * @throws IOException if a remote or network exception occurs    */
specifier|private
name|int
name|validateDBE
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|incompatibilities
init|=
literal|0
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Validating Data Block Encodings"
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getConf
argument_list|()
argument_list|)
init|;
name|Admin
name|admin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|List
argument_list|<
name|TableDescriptor
argument_list|>
name|tableDescriptors
init|=
name|admin
operator|.
name|listTableDescriptors
argument_list|()
decl_stmt|;
name|String
name|encoding
init|=
literal|""
decl_stmt|;
for|for
control|(
name|TableDescriptor
name|td
range|:
name|tableDescriptors
control|)
block|{
name|ColumnFamilyDescriptor
index|[]
name|columnFamilies
init|=
name|td
operator|.
name|getColumnFamilies
argument_list|()
decl_stmt|;
for|for
control|(
name|ColumnFamilyDescriptor
name|cfd
range|:
name|columnFamilies
control|)
block|{
try|try
block|{
name|encoding
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|cfd
operator|.
name|getValue
argument_list|(
name|DATA_BLOCK_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
comment|// IllegalArgumentException will be thrown if encoding is incompatible with 2.0
name|DataBlockEncoding
operator|.
name|valueOf
argument_list|(
name|encoding
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|incompatibilities
operator|++
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Incompatible DataBlockEncoding for table: {}, cf: {}, encoding: {}"
argument_list|,
name|td
operator|.
name|getTableName
argument_list|()
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|cfd
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|encoding
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|incompatibilities
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"There are {} column families with incompatible Data Block Encodings. Do not "
operator|+
literal|"upgrade until these encodings are converted to a supported one. "
operator|+
literal|"Check https://s.apache.org/prefixtree for instructions."
argument_list|,
name|incompatibilities
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"The used Data Block Encodings are compatible with HBase 2.0."
argument_list|)
expr_stmt|;
block|}
return|return
name|incompatibilities
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|printUsage
parameter_list|()
block|{
name|String
name|header
init|=
literal|"hbase "
operator|+
name|PreUpgradeValidator
operator|.
name|TOOL_NAME
operator|+
literal|" "
operator|+
name|PreUpgradeValidator
operator|.
name|VALIDATE_DBE_NAME
decl_stmt|;
name|printUsage
argument_list|(
name|header
argument_list|,
literal|null
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|(
name|validateDBE
argument_list|()
operator|==
literal|0
operator|)
condition|?
name|EXIT_SUCCESS
else|:
name|EXIT_FAILURE
return|;
block|}
block|}
end_class

end_unit

