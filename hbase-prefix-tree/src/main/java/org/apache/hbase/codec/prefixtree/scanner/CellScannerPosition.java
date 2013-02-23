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
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * An indicator of the state of the scanner after an operation such as nextCell() or  * positionAt(..). For example:  *<ul>  *<li>In a DataBlockScanner, the AFTER_LAST position indicates to the parent StoreFileScanner that  * it should load the next block.</li>  *<li>In a StoreFileScanner, the AFTER_LAST position indicates that the file has been exhausted.  *</li>  *<li>In a RegionScanner, the AFTER_LAST position indicates that the scanner should move to the  * next region.</li>  *</ul>  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
enum|enum
name|CellScannerPosition
block|{
comment|/**    * getCurrentCell() will NOT return a valid cell. Calling nextCell() will advance to the first    * cell.    */
name|BEFORE_FIRST
block|,
comment|/**    * getCurrentCell() will return a valid cell, but it is not the cell requested by positionAt(..),    * rather it is the nearest cell before the requested cell.    */
name|BEFORE
block|,
comment|/**    * getCurrentCell() will return a valid cell, and it is exactly the cell that was requested by    * positionAt(..).    */
name|AT
block|,
comment|/**    * getCurrentCell() will return a valid cell, but it is not the cell requested by positionAt(..),    * rather it is the nearest cell after the requested cell.    */
name|AFTER
block|,
comment|/**    * getCurrentCell() will NOT return a valid cell. Calling nextCell() will have no effect.    */
name|AFTER_LAST
block|}
end_enum

end_unit

