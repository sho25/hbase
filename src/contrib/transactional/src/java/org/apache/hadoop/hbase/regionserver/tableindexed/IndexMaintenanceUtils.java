begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|tableindexed
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
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
name|hbase
operator|.
name|ColumnNameParseException
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
name|HStoreKey
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
name|client
operator|.
name|tableindexed
operator|.
name|IndexSpecification
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
name|tableindexed
operator|.
name|IndexedTable
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
comment|/**  * Singleton class for index maintence logic.  */
end_comment

begin_class
specifier|public
class|class
name|IndexMaintenanceUtils
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IndexMaintenanceUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Put
name|createIndexUpdate
parameter_list|(
specifier|final
name|IndexSpecification
name|indexSpec
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|columnValues
parameter_list|)
block|{
name|byte
index|[]
name|indexRow
init|=
name|indexSpec
operator|.
name|getKeyGenerator
argument_list|()
operator|.
name|createIndexKey
argument_list|(
name|row
argument_list|,
name|columnValues
argument_list|)
decl_stmt|;
name|Put
name|update
init|=
operator|new
name|Put
argument_list|(
name|indexRow
argument_list|)
decl_stmt|;
name|update
operator|.
name|add
argument_list|(
name|IndexedTable
operator|.
name|INDEX_COL_FAMILY_NAME
argument_list|,
name|IndexedTable
operator|.
name|INDEX_BASE_ROW
argument_list|,
name|row
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
name|byte
index|[]
name|col
range|:
name|indexSpec
operator|.
name|getIndexedColumns
argument_list|()
control|)
block|{
name|byte
index|[]
name|val
init|=
name|columnValues
operator|.
name|get
argument_list|(
name|col
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected missing column value. ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|col
argument_list|)
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|byte
index|[]
index|[]
name|colSeperated
init|=
name|HStoreKey
operator|.
name|parseColumn
argument_list|(
name|col
argument_list|)
decl_stmt|;
name|update
operator|.
name|add
argument_list|(
name|colSeperated
index|[
literal|0
index|]
argument_list|,
name|colSeperated
index|[
literal|1
index|]
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|byte
index|[]
name|col
range|:
name|indexSpec
operator|.
name|getAdditionalColumns
argument_list|()
control|)
block|{
name|byte
index|[]
name|val
init|=
name|columnValues
operator|.
name|get
argument_list|(
name|col
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|!=
literal|null
condition|)
block|{
name|byte
index|[]
index|[]
name|colSeperated
init|=
name|HStoreKey
operator|.
name|parseColumn
argument_list|(
name|col
argument_list|)
decl_stmt|;
name|update
operator|.
name|add
argument_list|(
name|colSeperated
index|[
literal|0
index|]
argument_list|,
name|colSeperated
index|[
literal|1
index|]
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|ColumnNameParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|update
return|;
block|}
comment|/**      * Ask if this update does apply to the index.      *       * @param indexSpec      * @param columnValues      * @return true if possibly apply.      */
specifier|public
specifier|static
name|boolean
name|doesApplyToIndex
parameter_list|(
specifier|final
name|IndexSpecification
name|indexSpec
parameter_list|,
specifier|final
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|columnValues
parameter_list|)
block|{
for|for
control|(
name|byte
index|[]
name|neededCol
range|:
name|indexSpec
operator|.
name|getIndexedColumns
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|columnValues
operator|.
name|containsKey
argument_list|(
name|neededCol
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Index ["
operator|+
name|indexSpec
operator|.
name|getIndexId
argument_list|()
operator|+
literal|"] can't be updated because ["
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|neededCol
argument_list|)
operator|+
literal|"] is missing"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

