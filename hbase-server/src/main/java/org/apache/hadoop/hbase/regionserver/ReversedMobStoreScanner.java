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
name|regionserver
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
name|ArrayList
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
name|NavigableSet
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
name|Cell
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
name|Scan
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
name|mob
operator|.
name|MobCell
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
name|mob
operator|.
name|MobUtils
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

begin_comment
comment|/**  * ReversedMobStoreScanner extends from ReversedStoreScanner, and is used to support reversed  * scanning in both the memstore and the MOB store.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReversedMobStoreScanner
extends|extends
name|ReversedStoreScanner
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
name|ReversedMobStoreScanner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|cacheMobBlocks
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|rawMobScan
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|readEmptyValueOnMobCellMiss
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|HMobStore
name|mobStore
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|MobCell
argument_list|>
name|referencedMobCells
decl_stmt|;
name|ReversedMobStoreScanner
parameter_list|(
name|HStore
name|store
parameter_list|,
name|ScanInfo
name|scanInfo
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|long
name|readPt
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|store
argument_list|,
name|scanInfo
argument_list|,
name|scan
argument_list|,
name|columns
argument_list|,
name|readPt
argument_list|)
expr_stmt|;
name|cacheMobBlocks
operator|=
name|MobUtils
operator|.
name|isCacheMobBlocks
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|rawMobScan
operator|=
name|MobUtils
operator|.
name|isRawMobScan
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|readEmptyValueOnMobCellMiss
operator|=
name|MobUtils
operator|.
name|isReadEmptyValueOnMobCellMiss
argument_list|(
name|scan
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|store
operator|instanceof
name|HMobStore
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The store "
operator|+
name|store
operator|+
literal|" is not a HMobStore"
argument_list|)
throw|;
block|}
name|mobStore
operator|=
operator|(
name|HMobStore
operator|)
name|store
expr_stmt|;
name|this
operator|.
name|referencedMobCells
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|/**    * Firstly reads the cells from the HBase. If the cell is a reference cell (which has the    * reference tag), the scanner need seek this cell from the mob file, and use the cell found    * from the mob file as the result.    */
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|outResult
parameter_list|,
name|ScannerContext
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|result
init|=
name|super
operator|.
name|next
argument_list|(
name|outResult
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|rawMobScan
condition|)
block|{
comment|// retrieve the mob data
if|if
condition|(
name|outResult
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|result
return|;
block|}
name|long
name|mobKVCount
init|=
literal|0
decl_stmt|;
name|long
name|mobKVSize
init|=
literal|0
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
name|outResult
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Cell
name|cell
init|=
name|outResult
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|MobUtils
operator|.
name|isMobReferenceCell
argument_list|(
name|cell
argument_list|)
condition|)
block|{
name|MobCell
name|mobCell
init|=
name|mobStore
operator|.
name|resolve
argument_list|(
name|cell
argument_list|,
name|cacheMobBlocks
argument_list|,
name|readPt
argument_list|,
name|readEmptyValueOnMobCellMiss
argument_list|)
decl_stmt|;
name|mobKVCount
operator|++
expr_stmt|;
name|mobKVSize
operator|+=
name|mobCell
operator|.
name|getCell
argument_list|()
operator|.
name|getValueLength
argument_list|()
expr_stmt|;
name|outResult
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|mobCell
operator|.
name|getCell
argument_list|()
argument_list|)
expr_stmt|;
comment|// Keep the MobCell here unless we shipped the RPC or close the scanner.
name|referencedMobCells
operator|.
name|add
argument_list|(
name|mobCell
argument_list|)
expr_stmt|;
block|}
block|}
name|mobStore
operator|.
name|updateMobScanCellsCount
argument_list|(
name|mobKVCount
argument_list|)
expr_stmt|;
name|mobStore
operator|.
name|updateMobScanCellsSize
argument_list|(
name|mobKVSize
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|void
name|freeAllReferencedMobCells
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|MobCell
name|mobCell
range|:
name|referencedMobCells
control|)
block|{
name|mobCell
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|referencedMobCells
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shipped
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|shipped
argument_list|()
expr_stmt|;
name|this
operator|.
name|freeAllReferencedMobCells
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|freeAllReferencedMobCells
argument_list|()
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
name|warn
argument_list|(
literal|"Failed to free referenced mob cells: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

