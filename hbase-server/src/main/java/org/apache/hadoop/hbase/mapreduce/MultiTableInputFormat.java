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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|conf
operator|.
name|Configurable
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
name|conf
operator|.
name|Configuration
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

begin_comment
comment|/**  * Convert HBase tabular data from multiple scanners into a format that   * is consumable by Map/Reduce.  *  *<p>  * Usage example  *</p>  *  *<pre>  * List<Scan> scans = new ArrayList<Scan>();  *   * Scan scan1 = new Scan();  * scan1.setStartRow(firstRow1);  * scan1.setStopRow(lastRow1);  * scan1.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, table1);  * scans.add(scan1);  *  * Scan scan2 = new Scan();  * scan2.setStartRow(firstRow2);  * scan2.setStopRow(lastRow2);  * scan1.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, table2);  * scans.add(scan2);  *  * TableMapReduceUtil.initTableMapperJob(scans, TableMapper.class, Text.class,  *     IntWritable.class, job);  *</pre>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|MultiTableInputFormat
extends|extends
name|MultiTableInputFormatBase
implements|implements
name|Configurable
block|{
comment|/** Job parameter that specifies the scan list. */
specifier|public
specifier|static
specifier|final
name|String
name|SCANS
init|=
literal|"hbase.mapreduce.scans"
decl_stmt|;
comment|/** The configuration. */
specifier|private
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
comment|/**    * Returns the current configuration.    *    * @return The current configuration.    * @see org.apache.hadoop.conf.Configurable#getConf()    */
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
comment|/**    * Sets the configuration. This is used to set the details for the tables to    *  be scanned.    *    * @param configuration The configuration to set.    * @see org.apache.hadoop.conf.Configurable#setConf(    *        org.apache.hadoop.conf.Configuration)    */
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|configuration
expr_stmt|;
name|String
index|[]
name|rawScans
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|SCANS
argument_list|)
decl_stmt|;
if|if
condition|(
name|rawScans
operator|.
name|length
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"There must be at least 1 scan configuration set to : "
operator|+
name|SCANS
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Scan
argument_list|>
name|scans
init|=
operator|new
name|ArrayList
argument_list|<
name|Scan
argument_list|>
argument_list|()
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
name|rawScans
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|scans
operator|.
name|add
argument_list|(
name|TableMapReduceUtil
operator|.
name|convertStringToScan
argument_list|(
name|rawScans
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to convert Scan : "
operator|+
name|rawScans
index|[
name|i
index|]
operator|+
literal|" to string"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|this
operator|.
name|setScans
argument_list|(
name|scans
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

