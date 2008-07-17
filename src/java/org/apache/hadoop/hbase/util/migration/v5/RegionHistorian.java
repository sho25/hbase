begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|migration
operator|.
name|v5
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
name|text
operator|.
name|SimpleDateFormat
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|GregorianCalendar
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
name|HConstants
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
name|HServerAddress
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
name|io
operator|.
name|BatchUpdate
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * The Region Historian task is to keep track of every modification a region  * has to go through. Public methods are used to update the information in the  *<code>.META.</code> table and to retrieve it.  This is a Singleton.  By  * default, the Historian is offline; it will not log.  Its enabled in the  * regionserver and master down in their guts after there's some certainty the  * .META. has been deployed.  */
end_comment

begin_class
specifier|public
class|class
name|RegionHistorian
implements|implements
name|HConstants
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
name|RegionHistorian
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HTable
name|metaTable
decl_stmt|;
specifier|private
name|GregorianCalendar
name|cal
init|=
operator|new
name|GregorianCalendar
argument_list|()
decl_stmt|;
comment|/** Singleton reference */
specifier|private
specifier|static
name|RegionHistorian
name|historian
decl_stmt|;
comment|/** Date formater for the timestamp in RegionHistoryInformation */
specifier|private
specifier|static
name|SimpleDateFormat
name|dateFormat
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"EEE, d MMM yyyy HH:mm:ss"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|HistorianColumnKey
block|{
name|REGION_CREATION
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_FAMILY_HISTORIAN_STR
operator|+
literal|"creation"
argument_list|)
argument_list|)
block|,
name|REGION_OPEN
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_FAMILY_HISTORIAN_STR
operator|+
literal|"open"
argument_list|)
argument_list|)
block|,
name|REGION_SPLIT
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_FAMILY_HISTORIAN_STR
operator|+
literal|"split"
argument_list|)
argument_list|)
block|,
name|REGION_COMPACTION
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_FAMILY_HISTORIAN_STR
operator|+
literal|"compaction"
argument_list|)
argument_list|)
block|,
name|REGION_FLUSH
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_FAMILY_HISTORIAN_STR
operator|+
literal|"flush"
argument_list|)
argument_list|)
block|,
name|REGION_ASSIGNMENT
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COLUMN_FAMILY_HISTORIAN_STR
operator|+
literal|"assignment"
argument_list|)
argument_list|)
block|;
specifier|public
name|byte
index|[]
name|key
decl_stmt|;
name|HistorianColumnKey
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
block|}
comment|/**    * Default constructor. Initializes reference to .META. table.  Inaccessible.    * Use {@link #getInstance(HBaseConfiguration)} to obtain the Singleton    * instance of this class.    */
specifier|private
name|RegionHistorian
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get the RegionHistorian Singleton instance.    * @return The region historian    */
specifier|public
specifier|static
name|RegionHistorian
name|getInstance
parameter_list|()
block|{
if|if
condition|(
name|historian
operator|==
literal|null
condition|)
block|{
name|historian
operator|=
operator|new
name|RegionHistorian
argument_list|()
expr_stmt|;
block|}
return|return
name|historian
return|;
block|}
comment|/**    * Returns, for a given region name, an ordered list by timestamp of all    * values in the historian column of the .META. table.    * @param regionName    *          Region name as a string    * @return List of RegionHistoryInformation or null if we're offline.    */
specifier|public
name|List
argument_list|<
name|RegionHistoryInformation
argument_list|>
name|getRegionHistory
parameter_list|(
name|String
name|regionName
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isOnline
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|RegionHistoryInformation
argument_list|>
name|informations
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionHistoryInformation
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
comment|/*        * TODO REGION_HISTORIAN_KEYS is used because there is no other for the        * moment to retrieve all version and to have the column key information.        * To be changed when HTable.getRow handles versions.        */
for|for
control|(
name|HistorianColumnKey
name|keyEnu
range|:
name|HistorianColumnKey
operator|.
name|values
argument_list|()
control|)
block|{
name|byte
index|[]
name|columnKey
init|=
name|keyEnu
operator|.
name|key
decl_stmt|;
name|Cell
index|[]
name|cells
init|=
name|this
operator|.
name|metaTable
operator|.
name|get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|regionName
argument_list|)
argument_list|,
name|columnKey
argument_list|,
name|ALL_VERSIONS
argument_list|)
decl_stmt|;
if|if
condition|(
name|cells
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
name|informations
operator|.
name|add
argument_list|(
name|historian
operator|.
expr|new
name|RegionHistoryInformation
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|columnKey
argument_list|)
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
index|[
literal|1
index|]
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to retrieve region history"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|informations
argument_list|)
expr_stmt|;
return|return
name|informations
return|;
block|}
comment|/**    * Method to add a creation event to the row in the .META table    * @param info    */
specifier|public
name|void
name|addRegionAssignment
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|String
name|serverName
parameter_list|)
block|{
name|add
argument_list|(
name|HistorianColumnKey
operator|.
name|REGION_ASSIGNMENT
operator|.
name|key
argument_list|,
literal|"Region assigned to server "
operator|+
name|serverName
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
comment|/**    * Method to add a creation event to the row in the .META table    * @param info    */
specifier|public
name|void
name|addRegionCreation
parameter_list|(
name|HRegionInfo
name|info
parameter_list|)
block|{
name|add
argument_list|(
name|HistorianColumnKey
operator|.
name|REGION_CREATION
operator|.
name|key
argument_list|,
literal|"Region creation"
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
comment|/**    * Method to add a opening event to the row in the .META table    * @param info    * @param address    */
specifier|public
name|void
name|addRegionOpen
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|HServerAddress
name|address
parameter_list|)
block|{
name|add
argument_list|(
name|HistorianColumnKey
operator|.
name|REGION_OPEN
operator|.
name|key
argument_list|,
literal|"Region opened on server : "
operator|+
name|address
operator|.
name|getHostname
argument_list|()
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
comment|/**    * Method to add a split event to the rows in the .META table with    * information from oldInfo.    * @param oldInfo    * @param newInfo1     * @param newInfo2    */
specifier|public
name|void
name|addRegionSplit
parameter_list|(
name|HRegionInfo
name|oldInfo
parameter_list|,
name|HRegionInfo
name|newInfo1
parameter_list|,
name|HRegionInfo
name|newInfo2
parameter_list|)
block|{
name|HRegionInfo
index|[]
name|infos
init|=
operator|new
name|HRegionInfo
index|[]
block|{
name|newInfo1
block|,
name|newInfo2
block|}
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|info
range|:
name|infos
control|)
block|{
name|add
argument_list|(
name|HistorianColumnKey
operator|.
name|REGION_SPLIT
operator|.
name|key
argument_list|,
literal|"Region split from  : "
operator|+
name|oldInfo
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Method to add a compaction event to the row in the .META table    * @param info    */
specifier|public
name|void
name|addRegionCompaction
parameter_list|(
specifier|final
name|HRegionInfo
name|info
parameter_list|,
specifier|final
name|String
name|timeTaken
parameter_list|)
block|{
comment|// While historian can not log flushes because it could deadlock the
comment|// regionserver -- see the note in addRegionFlush -- there should be no
comment|// such danger compacting; compactions are not allowed when
comment|// Flusher#flushSomeRegions is run.
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|add
argument_list|(
name|HistorianColumnKey
operator|.
name|REGION_COMPACTION
operator|.
name|key
argument_list|,
literal|"Region compaction completed in "
operator|+
name|timeTaken
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Method to add a flush event to the row in the .META table    * @param info    */
specifier|public
name|void
name|addRegionFlush
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|String
name|timeTaken
parameter_list|)
block|{
comment|// Disabled.  Noop.  If this regionserver is hosting the .META. AND is
comment|// holding the reclaimMemcacheMemory global lock --
comment|// see Flusher#flushSomeRegions --  we deadlock.  For now, just disable
comment|// logging of flushes.
block|}
comment|/**    * Method to add an event with LATEST_TIMESTAMP.    * @param column    * @param text    * @param info    */
specifier|private
name|void
name|add
parameter_list|(
name|byte
index|[]
name|column
parameter_list|,
name|String
name|text
parameter_list|,
name|HRegionInfo
name|info
parameter_list|)
block|{
name|add
argument_list|(
name|column
argument_list|,
name|text
argument_list|,
name|info
argument_list|,
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
comment|/**    * Method to add an event with provided information.    * @param column    * @param text    * @param info    * @param timestamp    */
specifier|private
name|void
name|add
parameter_list|(
name|byte
index|[]
name|column
parameter_list|,
name|String
name|text
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isOnline
argument_list|()
condition|)
block|{
comment|// Its a noop
return|return;
block|}
if|if
condition|(
operator|!
name|info
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
name|BatchUpdate
name|batch
init|=
operator|new
name|BatchUpdate
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|batch
operator|.
name|setTimestamp
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
name|batch
operator|.
name|put
argument_list|(
name|column
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|text
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|metaTable
operator|.
name|commit
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to '"
operator|+
name|text
operator|+
literal|"'"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Inner class that only contains information about an event.    *     */
specifier|public
class|class
name|RegionHistoryInformation
implements|implements
name|Comparable
argument_list|<
name|RegionHistoryInformation
argument_list|>
block|{
specifier|private
name|long
name|timestamp
decl_stmt|;
specifier|private
name|String
name|event
decl_stmt|;
specifier|private
name|String
name|description
decl_stmt|;
specifier|public
name|RegionHistoryInformation
parameter_list|(
name|long
name|timestamp
parameter_list|,
name|String
name|event
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|event
operator|=
name|event
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
block|}
comment|/**      * Returns the inverse value of Long.compareTo      */
specifier|public
name|int
name|compareTo
parameter_list|(
name|RegionHistoryInformation
name|otherInfo
parameter_list|)
block|{
return|return
operator|-
literal|1
operator|*
name|Long
operator|.
name|valueOf
argument_list|(
name|timestamp
argument_list|)
operator|.
name|compareTo
argument_list|(
name|otherInfo
operator|.
name|getTimestamp
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|String
name|getEvent
parameter_list|()
block|{
return|return
name|event
return|;
block|}
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|description
return|;
block|}
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
comment|/**      * @return The value of the timestamp processed with the date formater.      */
specifier|public
name|String
name|getTimestampAsString
parameter_list|()
block|{
name|cal
operator|.
name|setTimeInMillis
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
return|return
name|dateFormat
operator|.
name|format
argument_list|(
name|cal
operator|.
name|getTime
argument_list|()
argument_list|)
return|;
block|}
block|}
comment|/**    * @return True if the historian is online. When offline, will not add    * updates to the .META. table.    */
specifier|public
name|boolean
name|isOnline
parameter_list|()
block|{
return|return
name|this
operator|.
name|metaTable
operator|!=
literal|null
return|;
block|}
comment|/**    * @param c Online the historian.  Invoke after cluster has spun up.    */
specifier|public
name|void
name|online
parameter_list|(
specifier|final
name|HBaseConfiguration
name|c
parameter_list|)
block|{
try|try
block|{
name|this
operator|.
name|metaTable
operator|=
operator|new
name|HTable
argument_list|(
name|c
argument_list|,
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Onlined"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to create RegionHistorian"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Offlines the historian.    * @see #online(HBaseConfiguration)    */
specifier|public
name|void
name|offline
parameter_list|()
block|{
name|this
operator|.
name|metaTable
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Offlined"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

