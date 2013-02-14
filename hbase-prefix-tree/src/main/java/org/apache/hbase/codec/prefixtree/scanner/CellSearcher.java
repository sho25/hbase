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
name|hbase
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|scanner
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|Cell
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
name|cell
operator|.
name|CellScannerPosition
import|;
end_import

begin_comment
comment|/**  * Methods for seeking to a random {@link Cell} inside a sorted collection of cells. Indicates that  * the implementation is able to navigate between cells without iterating through every cell.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|CellSearcher
extends|extends
name|ReversibleCellScanner
block|{
comment|/**    * Do everything within this scanner's power to find the key. Look forward and backwards.    *<p/>    * Abort as soon as we know it can't be found, possibly leaving the Searcher in an invalid state.    *<p/>    * @param key position the CellScanner exactly on this key    * @return true if the cell existed and getCurrentCell() holds a valid cell    */
name|boolean
name|positionAt
parameter_list|(
name|Cell
name|key
parameter_list|)
function_decl|;
comment|/**    * Same as positionAt(..), but go to the extra effort of finding the previous key if there's no    * exact match.    *<p/>    * @param key position the CellScanner on this key or the closest cell before    * @return AT if exact match<br/>    *         BEFORE if on last cell before key<br/>    *         BEFORE_FIRST if key was before the first cell in this scanner's scope    */
name|CellScannerPosition
name|positionAtOrBefore
parameter_list|(
name|Cell
name|key
parameter_list|)
function_decl|;
comment|/**    * Same as positionAt(..), but go to the extra effort of finding the next key if there's no exact    * match.    *<p/>    * @param key position the CellScanner on this key or the closest cell after    * @return AT if exact match<br/>    *         AFTER if on first cell after key<br/>    *         AFTER_LAST if key was after the last cell in this scanner's scope    */
name|CellScannerPosition
name|positionAtOrAfter
parameter_list|(
name|Cell
name|key
parameter_list|)
function_decl|;
comment|/**    * Note: Added for backwards compatibility with     * {@link org.apache.hadoop.hbase.regionserver.KeyValueScanner#reseek}    *<p/>    * Look for the key, but only look after the current position. Probably not needed for an    * efficient tree implementation, but is important for implementations without random access such    * as unencoded KeyValue blocks.    *<p/>    * @param key position the CellScanner exactly on this key    * @return true if getCurrent() holds a valid cell    */
name|boolean
name|seekForwardTo
parameter_list|(
name|Cell
name|key
parameter_list|)
function_decl|;
comment|/**    * Same as seekForwardTo(..), but go to the extra effort of finding the next key if there's no    * exact match.    *<p/>    * @param key    * @return AT if exact match<br/>    *         AFTER if on first cell after key<br/>    *         AFTER_LAST if key was after the last cell in this scanner's scope    */
name|CellScannerPosition
name|seekForwardToOrBefore
parameter_list|(
name|Cell
name|key
parameter_list|)
function_decl|;
comment|/**    * Same as seekForwardTo(..), but go to the extra effort of finding the next key if there's no    * exact match.    *<p/>    * @param key    * @return AT if exact match<br/>    *         AFTER if on first cell after key<br/>    *         AFTER_LAST if key was after the last cell in this scanner's scope    */
name|CellScannerPosition
name|seekForwardToOrAfter
parameter_list|(
name|Cell
name|key
parameter_list|)
function_decl|;
comment|/**    * Note: This may not be appropriate to have in the interface.  Need to investigate.    *<p/>    * Position the scanner in an invalid state after the last cell: CellScannerPosition.AFTER_LAST.    * This is used by tests and for handling certain edge cases.    */
name|void
name|positionAfterLastCell
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

