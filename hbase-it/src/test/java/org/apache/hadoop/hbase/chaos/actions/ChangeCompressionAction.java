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
name|chaos
operator|.
name|actions
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
name|Random
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
name|TableName
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
name|compress
operator|.
name|Compression
operator|.
name|Algorithm
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
name|compress
operator|.
name|Compressor
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

begin_comment
comment|/**  * Action that changes the compression algorithm on a column family from a list of tables.  */
end_comment

begin_class
specifier|public
class|class
name|ChangeCompressionAction
extends|extends
name|Action
block|{
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
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
name|ChangeCompressionAction
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|ChangeCompressionAction
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|random
operator|=
operator|new
name|Random
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|perform
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Possible compression algorithms. If an algorithm is not supported,
comment|// modifyTable will fail, so there is no harm.
name|Algorithm
index|[]
name|possibleAlgos
init|=
name|Algorithm
operator|.
name|values
argument_list|()
decl_stmt|;
comment|// Since not every compression algorithm is supported,
comment|// let's use the same algorithm for all column families.
comment|// If an unsupported compression algorithm is chosen, pick a different one.
comment|// This is to work around the issue that modifyTable() does not throw remote
comment|// exception.
name|Algorithm
name|algo
decl_stmt|;
do|do
block|{
name|algo
operator|=
name|possibleAlgos
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|possibleAlgos
operator|.
name|length
argument_list|)
index|]
expr_stmt|;
try|try
block|{
name|Compressor
name|c
init|=
name|algo
operator|.
name|getCompressor
argument_list|()
decl_stmt|;
comment|// call returnCompressor() to release the Compressor
name|algo
operator|.
name|returnCompressor
argument_list|(
name|c
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Performing action: Changing compression algorithms to "
operator|+
name|algo
operator|+
literal|" is not supported, pick another one"
argument_list|)
expr_stmt|;
block|}
block|}
do|while
condition|(
literal|true
condition|)
do|;
specifier|final
name|Algorithm
name|chosenAlgo
init|=
name|algo
decl_stmt|;
comment|// for use in lambda
name|LOG
operator|.
name|debug
argument_list|(
literal|"Performing action: Changing compression algorithms on "
operator|+
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|" to "
operator|+
name|chosenAlgo
argument_list|)
expr_stmt|;
name|modifyAllTableColumns
argument_list|(
name|tableName
argument_list|,
name|columnFamilyDescriptorBuilder
lambda|->
block|{
if|if
condition|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|columnFamilyDescriptorBuilder
operator|.
name|setCompactionCompressionType
argument_list|(
name|chosenAlgo
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|columnFamilyDescriptorBuilder
operator|.
name|setCompressionType
argument_list|(
name|chosenAlgo
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

